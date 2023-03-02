package org.kunlab.kpm;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenStoreTest
{
    private static String githubToken = null;

    @SneakyThrows(IOException.class)
    private static TokenStore createInstance()
    {
        Path tokenPath = File.createTempFile("test_token", ".dat").toPath();
        Path keyPath = File.createTempFile("test_key", ".dat").toPath();

        return new TokenStore(tokenPath, keyPath);
    }

    @BeforeAll
    @SuppressWarnings("unchecked")
    public static void injectEnv()
    {
        try
        {
            Class<?> cProcessEnvironment = Class.forName("java.lang.ProcessEnvironment");
            Field fTheCaseInsensitiveEnvironment = cProcessEnvironment.getDeclaredField("theCaseInsensitiveEnvironment");
            fTheCaseInsensitiveEnvironment.setAccessible(true);  // 黒魔術で, ENVを書き換える
            Map<String, String> env = (Map<String, String>) fTheCaseInsensitiveEnvironment.get(null);

            env.put("TOKEN", "dummy_token_114514");
        }
        catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e)
        {
            System.out.println("Failed to inject env");
        }
    }

    @BeforeAll
    public static void setGithubToken()
    {
        githubToken = System.getenv("GITHUB_TOKEN");
    }

    @AfterAll
    public static void wipeOldTokenFile()
    {
        File file = new File(new File("").getAbsoluteFile(), "kpm.vault");
        if (file.exists())
            file.delete();
        file = new File("dummy_key");
        if (file.exists())
            file.delete();
    }

    boolean isTokenSet()
    {
        return githubToken != null;
    }

    @Test
    void コンストラクタが正常に生成されるか()
    {
        assertDoesNotThrow(TokenStoreTest::createInstance);
    }

    @Test
    @Order(0)
    void 環境変数から読み込めるか()
    {
        TokenStore tokenStore = createInstance();
        tokenStore.fromEnv();

        assertEquals("dummy_token_114514", tokenStore.getToken());
    }

    @Test
    @Order(1)
    @EnabledIf("isTokenSet")
    @SneakyThrows(IOException.class)
    void トークンの生死チェックができるか()
    {
        TokenStore tokenStore = createInstance();
        tokenStore.storeToken(githubToken, false);  // トークンの自動チェックを無効化
        assertTrue(tokenStore.isTokenAlive());  // 与えられたトークンは有効であると仮定する
    }

    @Test
    @Order(2)
    void トークンがある場合にisTokenAvailableが動くか()
    {
        TokenStore tokenStore = createInstance();
        tokenStore.fromEnv();

        assertTrue(tokenStore.isTokenAvailable());
    }

    @Test
    @Order(2)
    void トークンを正常に取得できるか()
    {
        TokenStore tokenStore = createInstance();
        tokenStore.fromEnv();

        assertEquals("dummy_token_114514", tokenStore.getToken());
    }

    @Test
    @SneakyThrows(IOException.class)
    void tokenにディレクトリが入らないかどうか()
    {
        Path tokenPath = File.createTempFile("test_key", ".dat").toPath();
        Path keyPath = Files.createTempDirectory("test_");

        assertThrows(IllegalArgumentException.class, () -> new TokenStore(tokenPath, keyPath));
    }

    @Test
    @SneakyThrows(IOException.class)
    void keyにディレクトリが入らないかどうか()
    {
        Path tokenPath = Files.createTempDirectory("test_");
        Path keyPath = File.createTempFile("test_key", ".dat").toPath();

        assertThrows(IllegalArgumentException.class, () -> new TokenStore(tokenPath, keyPath));
    }

    @Test
    @EnabledIf("isTokenSet")
    @SneakyThrows(IOException.class)
    void トークンを保管し読み込みできるか()
    {
        TokenStore tokenStore = createInstance();
        tokenStore.storeToken(githubToken, false);

        // トークンを適当な文字列で上書きし、再読み込み（環境変数はインジェクション済み）
        tokenStore.fromEnv();
        tokenStore.loadToken();

        assertEquals(githubToken, tokenStore.getToken());
    }

    @Test
    void トークンを移行できるか() throws IOException
    {
        final String oldTokenContext = "old_token_114514";

        File file = new File(new File("").getAbsoluteFile(), "kpm.vault");
        if (!file.exists())
            file.createNewFile();

        // 適当な文字列を書き込み
        try (FileWriter fw = new FileWriter(file))
        {
            fw.write(oldTokenContext);
        }

        Path tokenPath = File.createTempFile("test_token", ".dat").toPath();
        Path keyPath = new File("dummy_key").toPath();  // キーの復元を阻止するため, 存在しないファイルが必要.

        TokenStore tokenStore = new TokenStore(tokenPath, keyPath);
        tokenStore.migrateToken();

        assertEquals(oldTokenContext, tokenStore.getToken());
    }

}

