package org.kunlab.kpm.alias;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.kunlab.kpm.alias.interfaces.Alias;
import org.kunlab.kpm.alias.interfaces.AliasProvider;
import org.kunlab.kpm.alias.interfaces.AliasUpdater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AliasProviderTest
{
    private AliasProvider provider = null;

    @BeforeEach
    void init() throws IOException
    {
        Path tempDB = Files.createTempFile("kpm_alias_test", ".sqlite");

        this.provider = new AliasProviderImpl(tempDB);
    }

    @AfterEach
    void close()
    {
        this.provider.close();
    }

    private Map<String, String> addAliases(int count)
    {
        String sourceName = "testSource";
        String sourceURL = "https://example.com/";
        Map<String, String> aliases = new HashMap<String, String>()
        {{
            for (int i = 0; i < count; i++)
            {
                UUID key = UUID.randomUUID();
                UUID value = UUID.randomUUID();
                this.put("testAlias" + i + "-" + key, "https://example.com/testAlias" + i + "-" + value);
            }
        }};

        AliasUpdater updater = this.provider.createUpdater(sourceName, sourceURL);

        for (Map.Entry<String, String> entry : aliases.entrySet())
            updater.update(entry.getKey(), entry.getValue());

        updater.done();

        return aliases;
    }

    private Map<String, String> addSources(int count)
    {
        Map<String, String> sources = new HashMap<String, String>()
        {{
            for (int i = 0; i < count; i++)
            {
                UUID key = UUID.randomUUID();
                UUID value = UUID.randomUUID();
                this.put("testSource" + i + "-" + key, "https://example.com/testSource" + i + "-" + value);
            }
        }};

        for (Map.Entry<String, String> entry : sources.entrySet())
        {
            AliasUpdater updater = this.provider.createUpdater(entry.getKey(), entry.getValue());
            updater.update("testAlias", "https://example.com/testAlias");
            updater.done();
        }

        return sources;
    }

    @Test
    void エイリアスが正しく格納されるか()
    {
        Map<String, String> aliases = this.addAliases(10);

        for (Map.Entry<String, String> entry : aliases.entrySet())
        {
            Alias alias = this.provider.getQueryByAlias(entry.getKey());
            assertNotNull(alias);

            assertEquals(entry.getKey(), alias.getAlias());
            assertEquals(entry.getValue(), alias.getQuery());
            assertEquals("testSource", alias.getSource());
        }
    }

    @Test
    void エイリアスのカウントができるか()
    {
        Map<String, String> aliases = this.addAliases(15);

        assertEquals(aliases.size(), this.provider.countAliases());
    }

    @Test
    void エイリアスのアップデートを中止できるか()
    {
        AliasUpdater updater = this.provider.createUpdater("testSource", "https://example.com/");

        updater.update("testAlias", "https://example.com/testAlias");
        updater.cancel();

        assertEquals(0, this.provider.countAliases());
    }

    @Test
    void エイリアスの存在チェックができるか()
    {
        Map<String, String> aliases = this.addAliases(10);

        for (Map.Entry<String, String> entry : aliases.entrySet())
            assertTrue(this.provider.hasAlias(entry.getKey()));
    }

    @Test
    void ソースが正しく格納されるか()
    {
        Map<String, String> sources = this.addSources(10);

        for (Map.Entry<String, String> entry : sources.entrySet())
            assertTrue(this.provider.hasSource(entry.getKey()));
    }
}
