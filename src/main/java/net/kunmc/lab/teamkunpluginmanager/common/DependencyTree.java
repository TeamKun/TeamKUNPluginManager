package net.kunmc.lab.teamkunpluginmanager.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.teamkunpluginmanager.console.PluginManagerConsole;
import net.kunmc.lab.teamkunpluginmanager.console.utils.PluginYamlParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DependencyTree
{
    private static HikariDataSource dataSource;

    static
    {
        initialize(PluginManagerConsole.config.getString("dependPath"));
    }

    private static void initialize(String fileName)
    {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");

        config.setJdbcUrl("jdbc:sqlite:" + new File(PluginManagerConsole.dataFolder.toFile(), fileName).getAbsolutePath());

        config.setMaximumPoolSize(20);
        config.setLeakDetectionThreshold(300000);

        dataSource = new HikariDataSource(config);
        initializeTable();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dataSource.close();
        }));
    }


    private static void initializeTable()
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

    public static void dropAll()
    {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement();)
        {
            st.execute("DELETE FROM DEPEND");
            st.execute("DELETE FROM DEPENDBY");
            st.execute("DELETE FROM PLUGIN");
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

    public static Info getInfo(String plugin, boolean allowEmpty)
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
            pluginSQL.setString(1, plugin);
            dependSQL.setString(1, plugin);
            dependBySQL.setString(1, plugin);

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

            ResultSet rdp = dependBySQL.executeQuery();
            ArrayList<Info.Depend> rdps = new ArrayList<>();
            while (rdp.next())
            {
                Info.Depend depend = new Info.Depend();
                depend.depend = rdp.getString("DEPENDBY");
                rdps.add(depend);
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

    public static void reCrawlAllPlugin()
    {
        dropAll();


        Path path;
        File file = (path = PluginManagerConsole.dataFolder.normalize().getParent()) == null ?
                PluginManagerConsole.dataFolder.normalize().toFile() : path.toFile();

        File[] files = file.listFiles((dir, name) -> name.endsWith(".jar"));

        if (files == null)
            return;

        Arrays.stream(files).forEach(file1 -> {
            try
            {
                PluginYamlParser pl = PluginYamlParser.fromJar(file1);
                DependencyTree.Info dInfo = new DependencyTree.Info();
                dInfo.name = pl.name;
                dInfo.version = pl.version;
                dInfo.depends = new ArrayList<>();
                Arrays.stream(pl.depend).parallel()
                        .forEach(s -> {
                            DependencyTree.Info.Depend dep = new DependencyTree.Info.Depend();
                            dep.name = s;
                            dInfo.depends.add(dep);
                        });

                crawlPlugin(dInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        });
    }

    public static void crawlPlugin(Info plugin)
    {
        if (plugin == null)
            return;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pluginSQL = con.prepareStatement("INSERT OR REPLACE INTO PLUGIN(PLUGIN, VERSION) VALUES (?,?)");
             PreparedStatement dependSQL = con.prepareStatement("INSERT OR REPLACE INTO DEPEND(PLUGIN, DEPEND) VALUES (?,?)");
             PreparedStatement dependBySQL = con.prepareStatement("INSERT OR REPLACE INTO DEPENDBY(PLUGIN, DEPENDBY) VALUES (?,?)")
        )
        {
            pluginSQL.setString(1, plugin.name);
            dependSQL.setString(1, plugin.name);
            dependBySQL.setString(2, plugin.name);

            pluginSQL.setString(2, plugin.version);
            pluginSQL.execute();

            plugin.depends
                    .forEach(depend -> {
                        try
                        {
                            dependSQL.setString(2, depend.name);
                            dependSQL.execute();

                            dependBySQL.setString(1, depend.name);
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
