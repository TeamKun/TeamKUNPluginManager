package org.kunlab.kpm.lang;

import lombok.Getter;
import lombok.Setter;
import org.kunlab.kpm.interfaces.KPMRegistry;

import java.io.IOException;
import java.util.Properties;

public class LanguageProvider
{
    private static LanguageProvider INSTANCE;

    @Getter
    private final KPMRegistry registry;
    private final LanguageLoader loader;

    @Setter
    private String currentLanguage;
    private Properties currentLanguageMessages;

    private LanguageProvider(KPMRegistry registry) throws IOException
    {
        this.registry = registry;
        this.loader = new LanguageLoader(this);

        LanguageProvider.INSTANCE = this;
    }

    public static void init(KPMRegistry registry) throws IOException
    {
        if (INSTANCE != null)
            throw new IllegalStateException("LanguageProvider has already been initialized.");
        new LanguageProvider(registry);
    }

    public static void setLanguage(String languageName) throws IOException
    {
        LanguageProvider provider = INSTANCE;
        provider.currentLanguage = languageName;
        provider.currentLanguageMessages = INSTANCE.loader.loadLanguage(languageName);
        provider.registry.getLogger().info(LanguageProvider.get("general.lang.set"));
    }

    public static String get(String key, MsgArgs args)
    {
        String msg = INSTANCE.currentLanguageMessages.getProperty(key);
        if (msg == null)
            throw new IllegalArgumentException("No such key \"" + key + "\" in language \"" + INSTANCE.currentLanguage + "\".");
        return args.format(msg);
    }

    public static String get(String key)
    {
        return get(key, MsgArgs.ofEmpty());
    }
}
