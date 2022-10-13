package net.kunmc.lab.teamkunpluginmanager.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.common.http.HTTPResponse;
import net.kunmc.lab.teamkunpluginmanager.common.http.RequestContext;
import net.kunmc.lab.teamkunpluginmanager.common.http.RequestMethod;
import net.kunmc.lab.teamkunpluginmanager.common.http.Requests;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
public class CommandRegister extends CommandBase
{
    private static final String CLIENT_ID = "94c5d446dbc765895979";
    private static final String OAUTH_SCOPE = "repo%20public_repo";
    private static final String OAUTH_PREPARE_URL =
            "https://github.com/login/device/code?client_id=" + CLIENT_ID + "&scope=" + OAUTH_SCOPE;
    private static final String OAUTH_ACCESS_URL =
            "https://github.com/login/oauth/access_token?client_id=" + CLIENT_ID +
                    "&grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code&device_code=";

    private final KPMDaemon daemon;

    private static void parseErrorAndPost(Terminal terminal, HTTPResponse response)
    {
        try
        {
            JsonObject json = response.getAsJson().getAsJsonObject();
            if (json.has("error"))
                terminal.error("エラーが発生しました。：Server response with " + parseError(json.get("error").getAsString()));
            else
                terminal.error("エラーが発生しました。：Server response with code " + response.getStatusCode());
        }
        catch (JsonSyntaxException | IllegalStateException e)
        {
            String responseString = response.getAsString();

            terminal.error("サーバから不正なデータを受信しました。：" +
                    responseString.substring(0, Math.min(responseString.length(), 50)));
        }
    }

    private void performAction(Terminal terminal)
    {
        terminal.info(ChatColor.LIGHT_PURPLE + "サーバと通信しています...");

        HTTPResponse response = Requests.request(RequestContext.builder()
                .method(RequestMethod.POST)
                .url(OAUTH_PREPARE_URL)
                .build());

        if (response.getStatus() != HTTPResponse.RequestStatus.OK)
        {
            parseErrorAndPost(terminal, response);
            return;
        }

        JsonObject object = response.getAsJson().getAsJsonObject();

        String deviceCode = object.get("device_code").getAsString();
        String userCode = object.get("user_code").getAsString();
        String verifyURI = object.get("verification_uri").getAsString();
        int expireSeconds = object.get("expires_in").getAsInt();
        int pollingInterval = object.get("interval").getAsInt();

        terminal.writeLine(ChatColor.DARK_GREEN + "こちらからコードを有効化してください：" + ChatColor.BLUE + ChatColor.UNDERLINE + verifyURI);
        terminal.writeLine(ChatColor.DARK_GREEN + "コード： " + ChatColor.WHITE + userCode);
        terminal.writeLine(ChatColor.DARK_GRAY + "I：なお、このコードは " + (expireSeconds / 60) + " 分で失効します。");
        if (terminal.isPlayer())
            terminal.showNotification(userCode, "GitHubでこのコードを入力して有効化してください。", expireSeconds * 20);
        //ここからポーリング

        AtomicBoolean successFlag = new AtomicBoolean(false);

        BukkitTask task = Runner.runTimerAsync(
                polling(terminal, deviceCode, successFlag), (exception, bukkitTask) -> bukkitTask.cancel(),
                100L, (pollingInterval * 20L) + 20L
        );

        Runner.runTimer(() -> {
            if (!successFlag.get())
                return;
            task.cancel();

            throw new RuntimeException();
        }, (e, t) -> t.cancel(), 10L * 20L);

    }

    private Runner.GeneralExceptableRunner polling(Terminal terminal, String device_code, AtomicBoolean successFlag)
    {
        return () -> {
            HTTPResponse httpResponse = Requests.request(RequestContext.builder()
                    .method(RequestMethod.POST)
                    .url(OAUTH_ACCESS_URL + device_code)
                    .build());

            if (httpResponse.getStatus() != HTTPResponse.RequestStatus.OK)
            {
                parseErrorAndPost(terminal, httpResponse);
                throw new RuntimeException(); // For cancel bukkit task
            }

            JsonObject response = httpResponse.getAsJson().getAsJsonObject();

            if (response.has("error"))
            {
                if (response.get("error").getAsString().equals("authorization_pending"))
                    return;

                terminal.error(
                        "エラーが発生しました。：Server response with %s",
                        parseError(response.get("error").getAsString())
                );

                if (terminal.isPlayer())
                    terminal.clearNotification();

                throw new RuntimeException(); // For cancel bukkit task
            }

            this.daemon.getTokenStore().storeToken(response.get("access_token").getAsString());
            terminal.success("トークンを正常に保管しました！");
            if (terminal.isPlayer())
                terminal.clearNotification();
            successFlag.set(true);

            throw new RuntimeException(); // For cancel bukkit task
        };
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (args.length < 1 && sender instanceof BlockCommandSender)
        {
            terminal.error("コマンドブロックから実行するには第一引数が必須です。");
            return;
        }

        if (args.length == 1)
        {
            try
            {
                this.daemon.getTokenStore().storeToken(args[0]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                terminal.error("トークンの保存に失敗しました。");
                return;
            }

            terminal.success("トークンを正常に保管しました！");
            return;
        }

        Runner.runAsync(() -> {
            QuestionResult result = terminal.getInput().
                    showQuestion("GitHubサーバを用いてトークンを生成しますか?", QuestionAttribute.YES, QuestionAttribute.CANCELLABLE)
                    .waitAndGetResult();

            if (result.test(QuestionAttribute.CANCELLABLE))
                terminal.error("キャンセルしました。");
            else if (result.test(QuestionAttribute.YES))
                this.performAction(terminal);

        }, (e, b) -> {
        });
    }

    private static String parseError(String err)
    {
        String response = err;

        switch (err)
        {
            case "expired_token":
            case "incorrect_device_code":
                response = "有効期限が切れています。もう一度お試しください。";
                break;
            case "access_denied":
                response = "キャンセルされました。";
                break;
        }

        return response;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.register";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("事前に取得したトークンを設定または、GitHubでログインしてトークンを設定します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("token", "string")
        };
    }
}
