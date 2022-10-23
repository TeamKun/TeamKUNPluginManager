package net.kunmc.lab.teamkunpluginmanager.kpminfo;

import net.kunmc.lab.teamkunpluginmanager.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.utils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
    public static KPMInformationFile load(InputStream stream) throws InvalidInformationFileException
    {
        return loadFromMap(YAML_PARSER.load(stream));
    }

    @NotNull
    public static KPMInformationFile load(Path jarFile) throws InvalidInformationFileException, FileNotFoundException
    {
        File file = jarFile.toFile();
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());

        try (ZipFile zip = new ZipFile(file))
        {
            InputStream stream = zip.getInputStream(zip.getEntry("kpm.yml"));
            if (stream == null)
                throw new InvalidInformationFileException("kpm.yml not found in " + file.getAbsolutePath());

            return load(stream);
        }
        catch (IOException e)
        {
            throw new InvalidInformationFileException("Failed to load kpm.yml from " + file.getAbsolutePath(), e);
        }
    }

    @NotNull
    private static KPMInformationFile loadFromMap(Map<?, ?> map) throws InvalidInformationFileException
    {
        if (map == null)
            throw new InvalidInformationFileException("Information file is empty.");

        Version version = parseVersion(map); // Parse kpm => kpmVersion [required]
        QueryContext updateQuery = parseUpdateQuery(map); // Parse update => updateQuery [required]

        return new KPMInformationFile(version, updateQuery);
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
    private static QueryContext parseUpdateQuery(Map<?, ?> map)
    {
        if (!map.containsKey("update"))
            return null;

        Object updateQueryObj = map.get("update");
        return QueryContext.fromString(updateQueryObj.toString());
    }
}
