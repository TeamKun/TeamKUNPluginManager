package net.kunmc.lab.teamkunpluginmanager.kpminfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.utils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 * KPMの情報ファイルを表すクラスです。
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KPMInformationFile
{
    private static final Yaml YAML_PARSER;

    static
    {
        YAML_PARSER = new Yaml();
    }

    /**
     * 対応するKPMのバージョンです。
     * このバージョンより古いKPMではこの情報ファイルを読み込むことができません。
     * YAMLのキーは{@code kpm}です。
     */
    Version kpmVersion;

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

    private static KPMInformationFile loadFromMap(Map<?, ?> map) throws InvalidInformationFileException
    {
        if (map == null)
            throw new InvalidInformationFileException("Information file is empty.");

        Version version;
        // region Parse kpm => kpmVersion [required]
        if (!map.containsKey("kpm"))
            throw new InvalidInformationFileException("kpm is not found.");

        Object kpmVersionObj = map.get("kpm");
        if (!(kpmVersionObj instanceof String))
            throw new InvalidInformationFileException("kpmVersion is not a string.");

        if ((version = Version.of((String) kpmVersionObj)) == null)
            throw new InvalidInformationFileException("Invalid syntax of kpm version: " + kpmVersionObj);
        // endregion

        return new KPMInformationFile(version);

    }

}
