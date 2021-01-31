package net.kunmc.lab.teamkunpluginmanager.plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class DependencyTree
{
    public static HikariDataSource dataSource;

    public static void initialize()
    {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");

        config.setJdbcUrl("jdbc:sqlite:" + TeamKunPluginManager.plugin.getDataFolder().getPath()  + TeamKunPluginManager.config.getString("dbPath"));

        config.setMaximumPoolSize(20);
        config.setLeakDetectionThreshold(300000);

        dataSource = new HikariDataSource(config);
    }

    public static void initializeTable()
    {
        try(Connection con = dataSource.getConnection();
                Statement stmt =  con.createStatement())
        {
            stmt.execute("CREATE TABLE IF NOT EXISTS PLUGIN(" +
                    "PLUGIN TEXT UNIQUE," +
                    "VERSION TEXT" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS DEPEND(" +
                    "PLUGIN TEXT," +
                    "DEPEND TEXT UNIQUE" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS DEPENDBY(" +
                    "PLUGIN TEXT," +
                    "DEPENDBY TEXT UNIQUE" +
                    ")");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void crawlAllPlugins()
    {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .forEach(DependencyTree::crawlPlugin);
    }


    public static void crawlPlugin(String name)
    {
        crawlPlugin(Bukkit.getPluginManager().getPlugin(name));
    }

    public static void wipePlugin(String name)
    {
        wipePlugin(Bukkit.getPluginManager().getPlugin(name));
    }

    public static void wipePlugin(Plugin plugin)
    {
        if (plugin == null)
            return;
        try(Connection con = dataSource.getConnection();
            PreparedStatement p = con.prepareStatement("DELETE FROM PLUGIN WHERE PLUGIN=?");
            PreparedStatement f = con.prepareStatement("DELETE FROM DEPEND WHERE PLUGIN=?");
            PreparedStatement b = con.prepareStatement("DELETE FROM DEPENDBY WHERE PLUGIN=?");
        )
        {
            p.setString(1, plugin.getName().toLowerCase());
            f.setString(1, plugin.getName().toLowerCase());
            b.setString(1, plugin.getName().toLowerCase());
            p.execute();
            f.execute();
            b.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void crawlPlugin(Plugin plugin)
    {
        if (plugin == null)
            return;
        try (Connection con = dataSource.getConnection();
                PreparedStatement pluginSQL = con.prepareStatement("INSERT OR REPLACE INTO PLUGIN(PLUGIN, VERSION) VALUES (?,?)");
             PreparedStatement dependSQL = con.prepareStatement("INSERT OR REPLACE INTO DEPEND(PLUGIN, DEPEND) VALUES (?,?)");
             PreparedStatement dependBySQL = con.prepareStatement("INSERT OR REPLACE INTO DEPENDBY(PLUGIN, DEPENDBY) VALUES (?,?)")
        )
        {
            pluginSQL.setString(1, plugin.getName().toLowerCase());
            dependSQL.setString(1, plugin.getName().toLowerCase());
            dependBySQL.setString(2, plugin.getName().toLowerCase());

            pluginSQL.setString(2, plugin.getDescription().getVersion());
            pluginSQL.execute();

            plugin.getDescription().getDepend()
                    .forEach(depend -> {
                        try
                        {
                            dependSQL.setString(2, depend.toLowerCase());
                            dependSQL.execute();

                            dependBySQL.setString(1, depend.toLowerCase());
                            dependBySQL.execute();
                        }
                        catch (SQLException throwable)
                        {
                            throwable.printStackTrace();
                        }
                    });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
