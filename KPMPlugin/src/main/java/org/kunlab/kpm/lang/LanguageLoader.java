package org.kunlab.kpm.lang;

import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.utils.PluginUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class LanguageLoader
{
    private static final Path LANGUAGES_DIR = Paths.get("lang");

    private final KPMRegistry registry;
    private final List<String> jarLanguages;
    private final List<String> dataFolderLanguages;

    public LanguageLoader(LanguageProvider provider) throws IOException
    {
        this.registry = provider.getRegistry();

        this.dataFolderLanguages = this.findDataFolderLanguages();
        this.jarLanguages = this.findJarLanguages();

        for (String lang : this.jarLanguages)
            if (this.dataFolderLanguages.contains(lang))
            {
                this.registry.getLogger().info("Duplicated language file \"" + lang + "\" in data folder " +
                        "will be overlapped by the one in jar file.");
                this.dataFolderLanguages.remove(lang);
            }
    }

    private static Properties loadLangIS(InputStream inputStream) throws IOException
    {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }

    private List<String> findDataFolderLanguages()
    {
        List<String> result = new ArrayList<>();

        File dataFolder = this.registry.getEnvironment().getPlugin().getDataFolder();
        File languagesFolder = new File(dataFolder, LANGUAGES_DIR.toString());
        if (!languagesFolder.exists())
            return result;

        File[] files = languagesFolder.listFiles();
        if (files == null)
            return result;

        for (File file : files)
        {
            if (file.isDirectory())
                continue;

            String fileName = file.getName();
            if (!fileName.endsWith(".lang"))
            {
                this.registry.getLogger().warning("Invalid language file name: " + fileName);
                continue;
            }

            result.add(fileName.substring(0, fileName.length() - 5));  // Remove ".lang"
        }

        if (result.isEmpty())
            this.registry.getLogger().warning("No language files found in the data folder.");
        else
            this.registry.getLogger().info("Found " + result.size() + " language files in the data folder.");

        return result;
    }

    public Properties loadLanguage(@NotNull String langName) throws IOException
    {
        if (!(this.jarLanguages.contains(langName) || this.dataFolderLanguages.contains(langName)))
            throw new IllegalArgumentException("No such language \"" + langName + "\".");

        boolean isInJar = this.jarLanguages.contains(langName);
        if (isInJar)
            return this.loadLanguageFromJar(langName);
        else
            return this.loadLanguageFromDataFolder(langName);
    }

    private Properties loadLanguageFromJar(String name) throws IOException
    {
        File pluginFile = PluginUtil.getFile(this.registry.getEnvironment().getPlugin());
        String languagePath = LANGUAGES_DIR.resolve(name).toString();

        try (ZipInputStream zipInputStream = new ZipInputStream(pluginFile.toURI().toURL().openStream()))
        {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null)
            {
                if (entry.isDirectory())
                    continue;

                String entryName = entry.getName();
                if (!(entryName.startsWith(languagePath) && entryName.endsWith(".lang")))
                    continue;

                break;
            }

            if (entry == null)
                throw new IllegalStateException("Language file \"" + name + "\" not found in the jar file.");

            return loadLangIS(zipInputStream);
        }
    }

    private Properties loadLanguageFromDataFolder(String name) throws IOException
    {
        File dataFolder = this.registry.getEnvironment().getPlugin().getDataFolder();
        File languageFile = new File(dataFolder, LANGUAGES_DIR.resolve(name).toString());

        try (InputStream inputStream = languageFile.toURI().toURL().openStream())
        {
            return loadLangIS(inputStream);
        }
    }

    private List<String> findJarLanguages() throws IOException
    {
        List<String> result = new ArrayList<>();

        File jarFile = PluginUtil.getFile(this.registry.getEnvironment().getPlugin());
        try (ZipFile zipFile = new ZipFile(jarFile))
        {
            Iterator<? extends ZipEntry> iterator = zipFile.stream().iterator();

            boolean anyLanguageFound = false;
            while (iterator.hasNext())
            {
                ZipEntry entry = iterator.next();
                if (entry.isDirectory())
                    continue;

                String entryName = entry.getName();

                if (!entryName.startsWith(LANGUAGES_DIR.toString()))
                    if (anyLanguageFound)
                        break;  // We have found all languages
                    else
                        continue;

                anyLanguageFound = true;

                String[] parts = entryName.split("/");
                if (parts.length != 2)
                {
                    this.registry.getLogger().warning("Invalid language file path: " + entryName);
                    continue;
                }

                String langName = parts[1];
                if (!langName.endsWith(".lang"))
                {
                    this.registry.getLogger().warning("Invalid language file name: " + langName);
                    continue;
                }

                result.add(langName.substring(0, langName.length() - 5));  // Remove ".lang"
            }

        }

        if (result.isEmpty())
            this.registry.getLogger().warning("No language files found in the jar file.");
        else
            this.registry.getLogger().info("Found " + result.size() + " language files in the jar file.");

        return result;
    }

}
