package org.kunlab.kpm;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@UtilityClass
public class Utils
{
    private static final long SIZE_UNIT_THRESHOLD = 1024;  // 1000byte ではなく 1024byte で切り捨てる
    private static final int HASH_BUFFER_SIZE = 1024;
    private static final String[] SIZE_UNITS = {
            "B",
            "kB",
            "MB",
            "GB",
            "TB",
            "PB",
            "EB",
            "ZB",
            "YB"
    };

    public static void printInstallStatistics(Terminal terminal, int added, int removed, int modified, int pending)
    {
        terminal.writeLine(LangProvider.get(
                "installer.operation.result",
                MsgArgs.of("add", added)
                        .add("remove", removed)
                        .add("modify", modified)
                        .add("pending", pending)
        ));
    }

    public static void printInstallStatistics(Terminal terminal, InstallResult<? extends Enum<?>> result)
    {
        printInstallStatistics(
                terminal,
                result.getInstalledCount(),
                result.getRemovedCount(),
                result.getUpgradedCount(),
                result.getPendingCount()
        );
    }

    public static String roundSizeUnit(long size)
    {
        int unit = 0;
        double dSize = size;

        while (dSize > SIZE_UNIT_THRESHOLD && unit < SIZE_UNITS.length - 1)
        {
            dSize /= SIZE_UNIT_THRESHOLD;
            unit++;
        }

        return String.format("%.2f %s", dSize, SIZE_UNITS[unit]);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static String getHash(Path path, String algo)
    {
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance(algo);
        }
        catch (NoSuchAlgorithmException e)
        {
            return "<No such algorithm: " + algo + ">";
        }

        try (FileInputStream fis = new FileInputStream(path.toFile());
             DigestInputStream dis = new DigestInputStream(fis, md))
        {
            byte[] buffer = new byte[HASH_BUFFER_SIZE];
            while (dis.read(buffer) != -1)
            {
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));

            return sb.toString();
        }
        catch (IOException e)
        {
            return "<IOException thrown: " + e.getMessage() + ">";
        }
    }
}
