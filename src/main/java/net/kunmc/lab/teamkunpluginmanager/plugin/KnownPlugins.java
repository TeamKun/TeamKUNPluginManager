package net.kunmc.lab.teamkunpluginmanager.plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class KnownPlugins
{
    public static HikariDataSource dataSource;

    public static void drop()
    {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement())
        {
            stmt.execute("DELETE FROM DEPEND");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void initialization(String fileName)
    {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");

        config.setJdbcUrl("jdbc:sqlite:" + TeamKunPluginManager.plugin.getDataFolder().getPath() + fileName);

        config.setMaximumPoolSize(20);
        config.setLeakDetectionThreshold(300000);

        dataSource = new HikariDataSource(config);
        initTables();
    }

    private static void initTables()
    {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("CREATE TABLE IF NOT EXISTS DEPEND(NAME TEXT, URL TEXT)");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static KnownPluginEntry getKnown(String name)
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM DEPEND WHERE NAME=?"))
        {
            pstmt.setString(1, name);
            ResultSet set = pstmt.executeQuery();
            if (!set.next())
                return null;
            KnownPluginEntry entry = new KnownPluginEntry();
            entry.name = set.getString("NAME");
            entry.url = set.getString("URL");
            set.close();
            return entry;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isKnown(String name)
    {
        return getKnown(name) != null;
    }

    public static void addKnownPlugin(KnownPluginEntry entry)
    {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT INTO DEPEND VALUES (?, ?)"))
        {
            pstmt.setString(1, entry.name);
            pstmt.setString(2, entry.url);
            pstmt.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
