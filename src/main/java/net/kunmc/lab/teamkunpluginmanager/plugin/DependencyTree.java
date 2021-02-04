package net.kunmc.lab.teamkunpluginmanager.plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyTree
{
    public static HikariDataSource dataSource;

    public static void initialize(String fileName)
    {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");

        config.setJdbcUrl("jdbc:sqlite:" + new File(TeamKunPluginManager.plugin.getDataFolder().getPath(), fileName).getAbsolutePath());

        config.setMaximumPoolSize(20);
        config.setLeakDetectionThreshold(300000);

        dataSource = new HikariDataSource(config);
    }

    public static Info getInfo(String plugin, boolean allowEmpty)
    {
        return getInfo(Bukkit.getPluginManager().getPlugin(plugin), allowEmpty);
    }

    public static Info getInfo(Plugin plugin, boolean allowEmpty)
    {
        if (plugin == null)
            return null;

        Info result = new Info();

        try (Connection con = dataSource.getConnection();
             PreparedStatement pluginSQL = con.prepareStatement("SELECT * FROM PLUGIN WHERE PLUGIN=?");
             PreparedStatement dependSQL = con.prepareStatement("SELECT * FROM DEPEND WHERE PLUGIN=?");
             PreparedStatement dependBySQL = con.prepareStatement("SELECT * FROM DEPENDBY WHERE PLUGIN=?")
        )
        {
            pluginSQL.setString(1, plugin.getName());
            dependSQL.setString(1, plugin.getName());
            dependBySQL.setString(1, plugin.getName());

            ResultSet pl = pluginSQL.executeQuery();
            while (pl.next())
            {
                result.name = pl.getString("PLUGIN");
                result.version = pl.getString("VERSION");
            }
            pl.close();

            if (result.name == null && !allowEmpty)
                return null;

            ResultSet dp = dependSQL.executeQuery();
            ArrayList<Info.Depend> dps = new ArrayList<>();
            while (dp.next())
            {
                Info.Depend depend = new Info.Depend();
                depend.depend = dp.getString("DEPEND");
                dps.add(depend);
            }
            result.depends = dps;
            dp.close();

            ResultSet rdp = dependSQL.executeQuery();
            ArrayList<Info.Depend> rdps = new ArrayList<>();
            while (rdp.next())
            {
                Info.Depend depend = new Info.Depend();
                depend.depend = rdp.getString("DEPEND");
                dps.add(depend);
            }
            result.rdepends = rdps;
            rdp.close();

            return result;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void initializeTable()
    {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement())
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
                    "PLUGIN TEXT UNIQUE," +
                    "DEPENDBY TEXT" +
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

    public static void wipeAllPlugin()
    {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement())
        {
            stmt.execute("DELETE FROM PLUGIN");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void wipePlugin(String name)
    {
        wipePlugin(Bukkit.getPluginManager().getPlugin(name));
    }

    public static void wipePlugin(Plugin plugin)
    {
        if (plugin == null)
            return;
        try (Connection con = dataSource.getConnection();
             PreparedStatement p = con.prepareStatement("DELETE FROM PLUGIN WHERE PLUGIN=?")
        )
        {
            p.setString(1, plugin.getName());
            p.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void purge(String name)
    {
        try (Connection con = dataSource.getConnection();
             PreparedStatement dp = con.prepareStatement("DELETE FROM DEPEND WHERE PLUGIN=?");
             PreparedStatement rdp = con.prepareStatement("DELETE FROM DEPENDBY WHERE PLUGIN=?"))
        {
            dp.setString(1, name);
            rdp.setString(1, name);
            dp.execute();
            rdp.execute();
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
            pluginSQL.setString(1, plugin.getName());
            dependSQL.setString(1, plugin.getName());
            dependBySQL.setString(2, plugin.getName());

            pluginSQL.setString(2, plugin.getDescription().getVersion());
            pluginSQL.execute();

            plugin.getDescription().getDepend()
                    .forEach(depend -> {
                        try
                        {
                            dependSQL.setString(2, depend);
                            dependSQL.execute();

                            dependBySQL.setString(1, depend);
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

    public static ArrayList<String> unusedPlugins()
    {
        ArrayList<String> result = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             Statement pluginSQL = con.createStatement())
        {
            ResultSet set = pluginSQL.executeQuery("SELECT * FROM DEPENDBY");
            while (set.next())
            {
                String name = set.getString("PLUGIN");       //Depend
                String dependBy = set.getString("DEPENDBY"); //Dependしてるプラグ

                Info info = getInfo(dependBy, false);

                if (info == null)
                    result.add(name);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isErrors()
    {
        ArrayList<String> plugin = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel().map(Plugin::getName).collect(Collectors.toCollection(ArrayList::new));

        try (Connection con = dataSource.getConnection();
             Statement pluginSQL = con.createStatement())
        {
            ResultSet set = pluginSQL.executeQuery("SELECT * FROM PLUGIN");
            while (set.next())
            {
                String name = set.getString("PLUGIN");
                if (!plugin.contains(name))
                    return true;
                plugin.remove(name);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return plugin.size() != 0;
    }

    public static void fix()
    {
        wipeAllPlugin();
        crawlAllPlugins();
    }

    public static class Info
    {
        public String name;
        public String version;
        public List<Depend> depends;
        public List<Depend> rdepends;

        public static class Depend
        {
            public String name;
            public String depend;
        }
    }
}
