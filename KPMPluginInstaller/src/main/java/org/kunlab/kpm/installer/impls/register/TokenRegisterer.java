package org.kunlab.kpm.installer.impls.register;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.http.HTTPResponse;
import org.kunlab.kpm.http.RequestContext;
import org.kunlab.kpm.http.RequestMethod;
import org.kunlab.kpm.http.Requests;
import org.kunlab.kpm.installer.AbstractInstaller;
import org.kunlab.kpm.installer.impls.register.pojos.UserVerificationCodeResponse;
import org.kunlab.kpm.installer.impls.register.pojos.VerificationSubmitPollingResponse;
import org.kunlab.kpm.installer.impls.register.signals.TokenCheckingSignal;
import org.kunlab.kpm.installer.impls.register.signals.TokenGenerateStartingSignal;
import org.kunlab.kpm.installer.impls.register.signals.TokenStoredSignal;
import org.kunlab.kpm.installer.impls.register.signals.UserDoesntCompleteVerifySignal;
import org.kunlab.kpm.installer.impls.register.signals.UserVerificationSuccessSignal;
import org.kunlab.kpm.installer.impls.register.signals.UserVerifyDeniedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeExpiredSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeReceivedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeRequestFailedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeRequestingSignal;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.TaskFailedException;

import java.io.IOException;

/**
 * GitHubのトークンを登録するインストーラーの実装です。
 * トークン登録は主に以下の流れで動作します。
 *
 * <ol>
 *     <li>{@link RegisterTasks#REQUESTING_USER_VERIFICATION_CODE} - ユーザ検証コードの要求しています。</li>
 *     <li>{@link RegisterTasks#POLLING_USER_VERIFICATION} - ユーザ検証コードの入力を待機します。</li>
 *     <li>{@link RegisterTasks#REGISTERING_TOKEN} - トークンの登録中です。</li>
 * </ol>
 */
public class TokenRegisterer extends AbstractInstaller<RegisterArgument, RegisterErrorCause, RegisterTasks>
{
    private static final String CLIENT_ID = "94c5d446dbc765895979";
    private static final String OAUTH_SCOPE = "repo&20public_repo";

    private static final String VERIFICATION_CODE_REQUEST_URL =
            "https://github.com/login/device/code";
    private static final String VERIFICATION_POLLING_URL = "https://github.com/login/oauth/access_token";

    private static final String VERIFICATION_CODE_REQUEST_PARM = "client_id=%s&scope=%s";
    private static final String VERIFICATION_POLLING_BODY_PARM =
            "client_id=%s&device_code=%s&grant_type=urn%%3Aietf%%3Aparams%%3Aoauth%%3Agrant-type%%3Adevice_code";
    private static final long TIME_WAIT_THRESHOLD = 500L;

    public TokenRegisterer(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(registry, signalHandler);
        this.progress.setCurrentTask(RegisterTasks.INITIALIZED);
    }

    @Override
    public InstallResult<RegisterTasks> execute(@NotNull RegisterArgument argument) throws TaskFailedException
    {
        // region Check if token is provided in argument
        if (argument.getToken() != null)  // Token is already prepared, so register it as is.
            if (this.registerToken(argument.getToken(), true))
                return this.success();
            else
                return this.error(RegisterErrorCause.INVALID_TOKEN);
        // endregion

        //  Token is not prepared, so request it from GitHub.

        // region Ask user to generate token
        TokenGenerateStartingSignal signal = new TokenGenerateStartingSignal();
        this.postSignal(signal);

        boolean doContinue = signal.isContinueGenerate();

        if (!doContinue)
            return this.error(RegisterErrorCause.GENERATE_CANCELLED);

        // endregion

        return this.generateToken();
    }

    private InstallResult<RegisterTasks> generateToken()
    {
        // region Request verification code
        this.progress.setCurrentTask(RegisterTasks.REQUESTING_USER_VERIFICATION_CODE);
        this.postSignal(new VerificationCodeRequestingSignal());
        UserVerificationCodeResponse userVerifyCodeResponse = this.requestVerificationCode();
        if (userVerifyCodeResponse == null)
            return this.error(RegisterErrorCause.VERIFICATION_CODE_REQUEST_FAILED);

        this.postSignal(new VerificationCodeReceivedSignal(userVerifyCodeResponse));

        // endregion

        String accessToken;
        String tokenType;
        String scope;
        // region Polling user verification

        this.progress.setCurrentTask(RegisterTasks.POLLING_USER_VERIFICATION);
        VerificationSubmitPollingResponse pollingResponse = this.pollVerifyCodeSubmit(userVerifyCodeResponse);
        if (pollingResponse == null)
            return this.error(RegisterErrorCause.VERIFICATION_FAILED);

        accessToken = pollingResponse.getAccessToken();
        tokenType = pollingResponse.getTokenType();
        scope = pollingResponse.getScope();

        // endregion

        this.postSignal(new UserVerificationSuccessSignal(accessToken, tokenType, scope));

        if (this.registerToken(accessToken, false))
            return this.success();
        else
            return this.error(RegisterErrorCause.IO_EXCEPTION_OCCURRED);
    }

    @Nullable
    private UserVerificationCodeResponse requestVerificationCode()
    {
        try (HTTPResponse response = Requests.request(
                RequestContext.builder()
                        .url(VERIFICATION_CODE_REQUEST_URL + "?" + String.format(VERIFICATION_CODE_REQUEST_PARM, CLIENT_ID, OAUTH_SCOPE))
                        .method(RequestMethod.POST)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .build()
        ))
        {
            if (!response.isSuccessful())
            {
                String errorMessage = this.getErrorMessage(response);
                int errorCode = response.getStatusCode().getCode();
                this.postSignal(new VerificationCodeRequestFailedSignal(
                        errorCode,
                        errorMessage
                ));

                return null;
            }

            return this.toUserVerificationCodeResponse(response);
        }
        catch (IOException e)
        {
            this.registry.getExceptionHandler().report(e);
            return null;
        }
    }

    private UserVerificationCodeResponse toUserVerificationCodeResponse(@NotNull HTTPResponse response)
    {
        JsonObject json = response.getAsJson().getAsJsonObject();
        if (json == null)
            return null;

        if (!json.has("device_code")
                || !json.has("user_code")
                || !json.has("verification_uri")
                || !json.has("expires_in")
                || !json.has("interval"))
            return null; // Invalid response.

        return response.getAsObject(UserVerificationCodeResponse.class);
    }

    @SneakyThrows(InterruptedException.class)
    private void randomSleep(long interval)
    {
        long sleepTime = interval + (long) (Math.random() * TIME_WAIT_THRESHOLD);
        Thread.sleep(sleepTime);
    }

    @Nullable
    private VerificationSubmitPollingResponse pollVerifyCodeSubmit(UserVerificationCodeResponse apiResponse)
    {
        long start = System.currentTimeMillis();
        long expiresIn = apiResponse.getExpiresIn() * 1000;
        long intervalMillis = apiResponse.getInterval() * 1000;

        while (true)
        {
            long current = System.currentTimeMillis();
            long remaining = expiresIn - (current - start);

            if (remaining <= 0)
            {
                this.postSignal(new VerificationCodeExpiredSignal(apiResponse.getUserCode()));
                return null;
            }

            try (HTTPResponse response = Requests.request(
                    RequestContext.builder()
                            .url(VERIFICATION_POLLING_URL + "?" +
                                    String.format(VERIFICATION_POLLING_BODY_PARM, CLIENT_ID, apiResponse.getDeviceCode()))
                            .method(RequestMethod.POST)
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .build()
            ))
            {
                JsonObject json = response.getAsJson().getAsJsonObject();
                boolean isError = json.has("error");
                if (isError)
                {
                    String error = json.get("error").getAsString();

                    switch (error)
                    {
                        case "authorization_pending":
                            this.postSignal(new UserDoesntCompleteVerifySignal(
                                    apiResponse.getUserCode(),
                                    apiResponse.getVerificationUrl(),
                                    expiresIn,
                                    remaining
                            ));
                            break;
                        case "slow_down":
                            if (json.has("interval"))
                                intervalMillis = json.get("interval").getAsLong() * 1000;
                            break;
                        case "expired_token":
                            this.postSignal(new VerificationCodeExpiredSignal(apiResponse.getUserCode()));
                            return null;
                        case "access_denied":
                            this.postSignal(new UserVerifyDeniedSignal(apiResponse.getUserCode()));
                            return null;
                        default:
                            this.registry.getLogger().warning("Unknown error occurred while polling user verification: " + error);
                            return null;
                    }

                    this.randomSleep(intervalMillis);
                    continue;
                }

                VerificationSubmitPollingResponse pollingResponse = this.toVerificationSubmitPollingResponse(response);
                if (pollingResponse != null)
                    return pollingResponse;
            }
            catch (IOException e)
            {
                this.registry.getExceptionHandler().report(e);
                return null;
            }

            this.randomSleep(intervalMillis);
        }
    }

    private VerificationSubmitPollingResponse toVerificationSubmitPollingResponse(HTTPResponse response)
    {
        JsonObject json = response.getAsJson().getAsJsonObject();
        if (json == null)
            return null;

        if (!json.has("access_token")
                || !json.has("token_type")
                || !json.has("scope"))
            return null; // Invalid response.

        return response.getAsObject(VerificationSubmitPollingResponse.class);
    }

    @NotNull
    private String getErrorMessage(HTTPResponse response)
    {
        JsonObject json = response.getAsJson().getAsJsonObject();
        if (json == null || !json.has("error"))
            return "unknown";

        return json.get("error").getAsString();
    }

    private boolean registerToken(@NotNull String token, boolean availableCheck)
    {
        this.progress.setCurrentTask(RegisterTasks.REGISTERING_TOKEN);

        try
        {
            if (availableCheck)
                this.postSignal(new TokenCheckingSignal());

            this.registry.getTokenStore().storeToken(token, availableCheck);
            this.postSignal(new TokenStoredSignal(token));

            return true;
        }
        catch (IOException e)
        {
            this.registry.getExceptionHandler().report(e);
            return false;
        }
        catch (IllegalStateException e)
        {
            return false;
        }
    }
}
