package org.kunlab.kpm;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.http.HTTPResponse;
import org.kunlab.kpm.http.RequestContext;
import org.kunlab.kpm.http.RequestMethod;
import org.kunlab.kpm.http.Requests;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * トークンを安全に保存するためのクラスです。
 */
public class TokenStore
{
    private static final String ALGORITHM = "AES";
    private static final String SALT = "TeamKunPluginManager>114514";
    private static final String SECONDARY_KEY = "KPM>Origin>114514";

    private static final int ALGO_KEY_SIZE = 256;
    private static final int ALGO_KEY_ITERATION = 65536;

    private final Path tokenPath;

    private final byte[] key;
    private final ExceptionHandler exceptionHandler;
    private final SecretKeySpec SECONDARY_KEY_SPEC;

    private String tokenCache;

    @SneakyThrows(IOException.class)
    public TokenStore(@NotNull Path tokenPath, @NotNull Path keyPath, @NotNull ExceptionHandler exceptionHandler)
    {
        if (Files.isDirectory(tokenPath) || Files.isDirectory(keyPath))
            throw new IllegalArgumentException("Path must be a file");

        this.tokenPath = tokenPath;
        this.key = this.getkey(keyPath);
        this.exceptionHandler = exceptionHandler;
        this.SECONDARY_KEY_SPEC = getKeySpec(SECONDARY_KEY);
    }

    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    private static SecretKeySpec getKeySpec(String key)
    {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), SALT.getBytes(), ALGO_KEY_ITERATION, ALGO_KEY_SIZE);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    @SneakyThrows(IOException.class)
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void setPermission(Path path)
    {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        {
            File f = path.toFile();
            f.setReadable(true, false);
            f.setWritable(true, false);
        }
        else
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
    }

    private boolean isTokenAlive(String token)
    {
        try (HTTPResponse apiResponse = Requests.request(RequestContext.builder()
                .url("https://api.github.com/rate_limit")
                .method(RequestMethod.GET)
                .header("Authorization", "Token " + token)
                .build()))
        {
            return apiResponse.isSuccessful();
        }
        catch (IOException e)
        {
            this.exceptionHandler.report(e);
            return false;
        }
    }

    private byte[] encryptToken(String token)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, this.SECONDARY_KEY_SPEC);
            byte[] encrypted = cipher.doFinal(token.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(this.key, ALGORITHM));

            return cipher.doFinal(encrypted);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to encrypt token.", e);
        }
    }

    private String decryptToken(byte[] encryptedToken)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(this.key, ALGORITHM));

            byte[] decrypted = cipher.doFinal(encryptedToken);

            cipher.init(Cipher.DECRYPT_MODE, this.SECONDARY_KEY_SPEC);

            return new String(cipher.doFinal(decrypted));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to decrypt token.", e);
        }
    }

    private byte[] getkey(Path keyPath) throws IOException
    {
        if (Files.exists(keyPath))
            return Files.readAllBytes(keyPath);

        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(ALGO_KEY_SIZE);
            SecretKey key = keyGenerator.generateKey();
            byte[] keyBytes = key.getEncoded();
            Files.write(keyPath, keyBytes);

            setPermission(keyPath);

            return keyBytes;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("Failed to generate key.", e);
        }
    }

    /**
     * トークンを保存します。
     *
     * @param token       トークン
     * @param checkLiving トークンが生きているか確認するかどうか
     */
    public void storeToken(String token, boolean checkLiving) throws IOException
    {
        if (checkLiving && !this.isTokenAlive(token))
            throw new IllegalStateException("Token is not available.");

        byte[] tokenBytes = this.encryptToken(token);
        long time = System.currentTimeMillis();
        String tokenBody = Base64.getEncoder().encodeToString(tokenBytes);
        String tokenString = String.format(
                "%s,%s",
                tokenBody,
                time
        );

        Files.write(this.tokenPath, tokenString.getBytes());

        setPermission(this.tokenPath);

        this.tokenCache = token;
    }

    /**
     * トークンをファイルから読み込みます。
     *
     * @return トークンの読み取りに成功したかどうか
     * @throws IOException トークンの読み取りに失敗した場合(ファイルが存在しない場合は{@code false}を返します)
     */
    public boolean loadToken() throws IOException
    {
        if (!Files.exists(this.tokenPath))
            return false;

        String tokenString = new String(Files.readAllBytes(this.tokenPath));

        String[] tokenParts = tokenString.split(",");
        if (tokenParts.length != 2)
            return false;

        String tokenBody = tokenParts[0];
        this.tokenCache = this.decryptToken(Base64.getDecoder().decode(tokenBody));

        return true;
    }

    /**
     * トークンを KPMv2 の形式から移行します。
     *
     * @return 移行に成功したかどうか
     * @throws IOException 移行に失敗した場合
     */
    public boolean migrateToken() throws IOException
    {
        File oldToken = new File(new File("").getAbsoluteFile(), "kpm.vault");

        if (!oldToken.exists())
            return false;

        String token = new String(Files.readAllBytes(oldToken.toPath()));
        this.storeToken(token, false);

        //noinspection ResultOfMethodCallIgnored
        oldToken.delete();

        return true;
    }

    /**
     * トークンが利用可能かどうかを返します。
     *
     * @return トークンが利用可能かどうか
     */
    public boolean isTokenAvailable()
    {
        return this.tokenCache != null;
    }

    /**
     * トークンを取得します。また、ロードされていない場合はロードします。
     *
     * @return トークン
     */
    public String getToken()
    {
        if (this.tokenCache == null)
        {
            try
            {
                this.loadToken();
            }
            catch (IOException e)
            {
                this.exceptionHandler.report(e);
            }
        }
        return this.tokenCache;
    }

    /**
     * トークンを環境変数 {@code TOKEN} から取得します。
     */
    public void fromEnv()
    {
        String token = System.getenv("TOKEN");
        if (token != null)
            this.tokenCache = token;
    }

    /**
     * トークンが有効かどうかを GitHub API で確認します。
     */
    public boolean isTokenAlive()
    {
        if (this.tokenCache == null)
            return false;

        return this.isTokenAlive(this.tokenCache);
    }
}
