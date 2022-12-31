package net.kunmc.lab.kpm;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@UtilityClass
public class Utils
{
    private static final String STATS_FORMAT
            = ChatColor.GREEN + "%d 追加 " + ChatColor.RED + "%d 削除 " + ChatColor.YELLOW + "%d 変更 " + ChatColor.GRAY + "%d 保留";
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

    public static void printInstallStatistics(Terminal terminal, int added, int removed, int changed, int pending)
    {
        terminal.writeLine(String.format(STATS_FORMAT, added, removed, changed, pending));
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

        while (dSize > 1024 && unit < SIZE_UNITS.length - 1)
        {
            dSize /= 1024;
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
            byte[] buffer = new byte[1024];
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
