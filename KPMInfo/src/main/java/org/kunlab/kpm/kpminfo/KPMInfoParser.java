package org.kunlab.kpm.kpminfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.hook.HookRecipientListImpl;
import org.kunlab.kpm.hook.interfaces.HookRecipientList;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.resolver.QueryContextParser;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.versioning.Version;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class KPMInfoParser
{
    private static final Yaml YAML_PARSER;

    static
    {
        YAML_PARSER = new Yaml();
    }

    @NotNull
    public static KPMInformationFile loadFromMap(@NotNull KPMRegistry registry, Map<?, ?> map) throws InvalidInformationFileException
    {
        if (map == null)
            throw new InvalidInformationFileException("Information file is empty.");

        Version version = parseVersion(map); // Parse kpm => kpmVersion [required]
        QueryContext updateQuery = parseUpdateQuery(map); // Parse update => updateQuery [required]
        HookRecipientList hooks = parseHooks(registry, map); // Parse hooks [optional]
        String[] recipes = parseRecipes(map); // Parse recipes [optional]
        Map<String, QueryContext> dependencies = parseDependencies(map); // Parse dependencies [optional]
        boolean allowManuallyInstall = parseAllowManuallyInstall(map); // Parse allowManuallyInstall [optional: true]


        return new KPMInformationFileImpl(version, updateQuery, hooks, recipes, dependencies, allowManuallyInstall);
    }

    @NotNull
    private static Version parseVersion(Map<?, ?> map) throws InvalidInformationFileException
    {
        if (map.containsKey("kpm"))
        {
            Object kpmVersionObj = map.get("kpm");
            if (!Version.isValidVersionString(kpmVersionObj.toString()))
                throw new InvalidInformationFileException("Invalid syntax of kpm version: " + kpmVersionObj);
            return Version.of((String) kpmVersionObj);
        }
        else
            throw new InvalidInformationFileException("kpm is not found.");

    }

    @Nullable
    private static QueryContext parseUpdateQuery(Map<?, ?> map) throws InvalidInformationFileException
    {
        if (!map.containsKey("update"))
            return null;

        String updateQuery = map.get("update").toString();
        if (updateQuery.isEmpty())
            throw new InvalidInformationFileException("Update query is empty.");

        return QueryContextParser.fromString(updateQuery);
    }

    @NotNull
    private static HookRecipientList parseHooks(KPMRegistry registry, Map<?, ?> map) throws InvalidInformationFileException
    {
        HookRecipientList result = new HookRecipientListImpl(registry, registry.getHookExecutor());

        if (!map.containsKey("hooks"))
            return result;

        Object hooksObj = map.get("hooks");
        if (!(hooksObj instanceof List))
            throw new InvalidInformationFileException("hooks must be a list of full-qualified class names.");

        List<?> hooks = (List<?>) hooksObj;

        for (Object hook : hooks)
        {
            if (!(hook instanceof String))
                throw new InvalidInformationFileException("hooks must be a list of full-qualified class names.");

            result.add(hook.toString());
        }

        return result;
    }

    private static String[] parseRecipes(Map<?, ?> map) throws InvalidInformationFileException
    {
        if (!map.containsKey("recipes"))
            return new String[0];

        Object recipesObj = map.get("recipes");
        if (!(recipesObj instanceof List))
            throw new InvalidInformationFileException("recipes must be a list of strings.");

        List<?> recipes = (List<?>) recipesObj;
        String[] result = new String[recipes.size()];

        for (int i = 0; i < recipes.size(); i++)
        {
            Object recipe = recipes.get(i);
            if (!(recipe instanceof String))
                throw new InvalidInformationFileException("recipes must be a list of strings.");

            result[i] = recipe.toString();
        }

        return result;
    }

    private static Map<String, QueryContext> parseDependencies(Map<?, ?> map) throws InvalidInformationFileException
    {
        Map<String, QueryContext> result = new HashMap<>();

        if (!map.containsKey("dependencies"))
            return result;

        Object dependenciesObj = map.get("dependencies");
        if (!(dependenciesObj instanceof Map))
            throw new InvalidInformationFileException("dependencies must be a map of strings to strings.");

        Map<?, ?> dependencies = (Map<?, ?>) dependenciesObj;

        for (Map.Entry<?, ?> entry : dependencies.entrySet())
        {
            if (!(entry.getKey() instanceof String && entry.getValue() instanceof String))
                throw new InvalidInformationFileException("dependencies must be a map of strings to strings.");

            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            try
            {
                result.put(key, QueryContextParser.fromString(value));
            }
            catch (IllegalArgumentException e)
            {
                throw new InvalidInformationFileException("Invalid syntax of dependency query: " + value);
            }
        }

        return result;
    }

    private static boolean parseAllowManuallyInstall(Map<?, ?> map)
    {
        return !map.containsKey("allowManuallyInstall") ||
                map.get("allowManuallyInstall") instanceof Boolean && (Boolean) map.get("allowManuallyInstall");
    }

    @NotNull
    public static KPMInformationFile load(@NotNull KPMRegistry registry, @NotNull InputStream stream) throws InvalidInformationFileException
    {
        try
        {
            return KPMInfoParser.loadFromMap(registry, KPMInfoParser.YAML_PARSER.load(stream));
        }
        catch (YAMLException e)
        {
            registry.getLogger().warning("Failed to load kpm info file: " + e.getMessage());
            throw new InvalidInformationFileException("Load failed", e);
        }
    }

    @NotNull
    public static KPMInformationFile load(@NotNull KPMRegistry registry, @NotNull Path jarFile) throws InvalidInformationFileException, FileNotFoundException
    {
        File file = jarFile.toFile();
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());

        try (ZipFile zip = new ZipFile(file))
        {
            ZipEntry kpmFileEntry = zip.getEntry("kpm.yml");
            if (kpmFileEntry == null)
                throw new FileNotFoundException("kpm.yml not found in " + file.getAbsolutePath());

            InputStream stream = zip.getInputStream(kpmFileEntry);

            return load(registry, stream);
        }
        catch (FileNotFoundException e)
        {
            throw e;  // Pass to upstream
        }
        catch (IOException e)
        {
            throw new InvalidInformationFileException("Failed to load kpm.yml from " + file.getAbsolutePath(), e);
        }
    }
}
