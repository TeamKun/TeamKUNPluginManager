package org.kunlab.kpm.lang;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.kunlab.kpm.interfaces.KPMRegistry;

import java.io.IOException;
import java.util.Properties;

public class LangProvider
{
    private static LangProvider INSTANCE;

    @Getter
    private final KPMRegistry registry;
    private final LangLoader loader;

    @Setter
    private String currentLanguage;
    private Properties currentLanguageMessages;

    private LangProvider(KPMRegistry registry) throws IOException
    {
        this.registry = registry;
        this.loader = new LangLoader(this);

        LangProvider.INSTANCE = this;
    }

    public static void init(KPMRegistry registry) throws IOException
    {
        if (INSTANCE != null)
            throw new IllegalStateException("LanguageProvider has already been initialized.");
        new LangProvider(registry);
    }

    public static void setLanguage(String languageName) throws IOException
    {
        LangProvider provider = INSTANCE;
        provider.currentLanguage = languageName;
        provider.currentLanguageMessages = INSTANCE.loader.loadLanguage(languageName);

        buildCache();
        provider.registry.getLogger().info(LangProvider.get("general.lang.set"));
    }

    public static void buildCache()
    {
        Properties current = INSTANCE.currentLanguageMessages;
        Properties cache = new Properties();
        current.keySet().stream().parallel()
                .map(Object::toString)
                .map(key -> Pair.of(key, get(key)))
                .forEach(pair -> cache.setProperty(pair.getLeft(), pair.getRight()));

        INSTANCE.currentLanguageMessages = cache;
    }

    public static String get(String key, MsgArgs args)
    {
        String msg = INSTANCE.currentLanguageMessages.getProperty(key);
        if (msg == null)
            return "%%" + key + "%%";
        return args.format(msg);
    }

    public static String get(String key)
    {
        return get(key, MsgArgs.ofEmpty());
    }

    public static TextComponent getComponent(String key, MsgArgs args)
    {
        return Component.text(get(key, args));
    }

    public static TextComponent getComponent(String key)
    {
        return getComponent(key, MsgArgs.ofEmpty());
    }
}
