package net.kunmc.lab.teamkunpluginmanager.plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class KnownPlugins
{
    public static HikariDataSource dataSource;

    public static void del(String source)
    {
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement("DELETE FROM PLUGIN WHERE SOURCE=?"))
        {
            pstmt.setString(1, source);
            pstmt.execute();
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

        config.setJdbcUrl("jdbc:sqlite:" + new File(TeamKunPluginManager.plugin.getDataFolder().getPath(), fileName).getAbsolutePath());

        config.setMaximumPoolSize(20);
        config.setLeakDetectionThreshold(300000);

        dataSource = new HikariDataSource(config);
        if (isLegacy())
            return;
        initTables();
    }

    public static boolean isLegacy()
    {
        try(Connection connection = dataSource.getConnection();)
        {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet set = meta.getTables(null, null, "DEPEND", null);
            boolean a = set.next();
            set.close();
            return a;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static void migration()
    {
        if (!isLegacy())
            return;

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("DROP TABLE DEPEND");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        initTables();

    }

    private static void initTables()
    {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement())
        {
            stmt.execute("CREATE TABLE IF NOT EXISTS PLUGIN(" +
                    "NAME TEXT, " +
                    "URL TEXT, " +
                    "SOURCE TEXT)");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static KnownPluginEntry getKnown(String name)
    {
        String[] org = StringUtils.split(name, "/");
        String query = "SELECT * FROM PLUGIN WHERE " +
                "NAME=? ";
        if (org.length == 2)
            query += "AND SOURCE=?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query))
        {
            pstmt.setString(1, org[0]);
            if (org.length == 2)
                pstmt.setString(2, org[1]);
            ResultSet set = pstmt.executeQuery();
            if (!set.next())
                return null;
            KnownPluginEntry entry = new KnownPluginEntry();
            entry.name = set.getString("NAME");
            entry.url = set.getString("URL");
            entry.source = set.getString("SOURCE");
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
             PreparedStatement pstmt = connection.prepareStatement("INSERT INTO PLUGIN VALUES (?, ?, ?)"))
        {
            pstmt.setString(1, entry.name);
            pstmt.setString(2, entry.url);
            pstmt.setString(3, entry.source);
            pstmt.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
