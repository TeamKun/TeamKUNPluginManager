package net.kunmc.lab.teamkunpluginmanager;

import org.jetbrains.annotations.NotNull;

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

    private final Path tokenPath;

    private final byte[] key;
    private final SecretKeySpec SECONDARY_KEY_SPEC;

    private String tokenCache;

    public TokenStore(@NotNull Path tokenPath, @NotNull Path keyPath)
    {
        this.tokenPath = tokenPath;
        try
        {
            this.key = this.getkey(keyPath);
            this.SECONDARY_KEY_SPEC = getKeySpec(SECONDARY_KEY);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static SecretKeySpec getKeySpec(String key)
    {
        try
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), SALT.getBytes(), 65536, 256);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            // Never happen
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void setPermission(Path path)
    {
        try
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
        catch (IOException e)
        {
            throw new RuntimeException(e);
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
            keyGenerator.init(256);
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
     * @param token トークン
     */
    public void storeToken(String token) throws IOException
    {
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
        this.storeToken(token);

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
                System.out.println("Failed to load token.");
                throw new RuntimeException(e);
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
}
