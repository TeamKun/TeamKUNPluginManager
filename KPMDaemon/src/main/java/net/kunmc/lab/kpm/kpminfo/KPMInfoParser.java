package net.kunmc.lab.kpm.kpminfo;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.hook.HookRecipientList;
import net.kunmc.lab.kpm.hook.KPMHookRecipient;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.utils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class KPMInfoParser
{
    private static final Yaml YAML_PARSER;

    static
    {
        YAML_PARSER = new Yaml();
    }

    @NotNull
    public static KPMInformationFile load(@NotNull KPMDaemon daemon, @NotNull InputStream stream) throws InvalidInformationFileException
    {
        return loadFromMap(daemon, YAML_PARSER.load(stream));
    }

    @NotNull
    public static KPMInformationFile load(@NotNull KPMDaemon daemon, @NotNull Path jarFile) throws InvalidInformationFileException, FileNotFoundException
    {
        File file = jarFile.toFile();
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());

        try (ZipFile zip = new ZipFile(file))
        {
            InputStream stream = zip.getInputStream(zip.getEntry("kpm.yml"));
            if (stream == null)
                throw new InvalidInformationFileException("kpm.yml not found in " + file.getAbsolutePath());

            return load(daemon, stream);
        }
        catch (IOException e)
        {
            throw new InvalidInformationFileException("Failed to load kpm.yml from " + file.getAbsolutePath(), e);
        }
    }

    @NotNull
    private static KPMInformationFile loadFromMap(@NotNull KPMDaemon daemon, Map<?, ?> map) throws InvalidInformationFileException
    {
        if (map == null)
            throw new InvalidInformationFileException("Information file is empty.");

        Version version = parseVersion(map); // Parse kpm => kpmVersion [required]
        QueryContext updateQuery = parseUpdateQuery(map); // Parse update => updateQuery [required]
        HookRecipientList hooks = parseHooks(daemon, map); // Parse hooks [optional]

        return new KPMInformationFile(version, updateQuery, hooks);
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

        return QueryContext.fromString(updateQuery);
    }

    @NotNull
    private static HookRecipientList parseHooks(KPMDaemon daemon, Map<?, ?> map) throws InvalidInformationFileException
    {
        HookRecipientList result = new HookRecipientList(daemon.getHookExecutor());

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

            String hookClassName = (String) hook;
            try
            {
                Class<?> hookClass = Class.forName(hookClassName);
                if (!KPMHookRecipient.class.isAssignableFrom(hookClass))
                    throw new InvalidInformationFileException("Class " + hookClassName + " is not a KPMHookRecipient.");

                Constructor<? extends KPMHookRecipient> constructor =
                        hookClass.asSubclass(KPMHookRecipient.class).getConstructor(KPMDaemon.class);

                result.add(constructor.newInstance(daemon));
            }
            catch (ClassNotFoundException e)
            {
                throw new InvalidInformationFileException("Hook recipient class was not found: " + hookClassName, e);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                throw new InvalidInformationFileException("Failed to create an instance of hook recipient class: " +
                        hookClassName, e);
            }
            catch (NoSuchMethodException e)
            {
                throw new InvalidInformationFileException("Hook recipient class must have a constructor with" +
                        " a single parameter of type KPMDaemon: " + hookClassName, e);
            }
        }

        return result;
    }

}
