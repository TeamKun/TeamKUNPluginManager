package net.kunmc.lab.teamkunpluginmanager;

import develop.p2p.lib.FileConfiguration;
import org.bukkit.Bukkit;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.HashMap;

public class TokenVault
{
    private final FileConfiguration configuration;

    public TokenVault()
    {
        this.configuration = new FileConfiguration(new File(""), "kpm.cnf");
        this.configuration.saveDefaultConfig();

    }


    @SuppressWarnings("unchecked")
    public synchronized void vault(String token)
    {
        try(FileWriter wr = new FileWriter(new File(".", "kpm.cnf")))
        {
            Field tokenNF = FileConfiguration.class.getDeclaredField("config");
            tokenNF.setAccessible(true);
            HashMap<String, Object> tkg = (HashMap<String, Object>) tokenNF.get(this.configuration);
            tkg.put("oauth", token);
            wr.write(new Yaml().dump(tkg));
        }
        catch (Exception e)
        {
            System.out.println("TOKENの保管に失敗しました。");
        }
    }

    public String getToken()
    {
        if (this.configuration.getString("oauth") == null)
            return "";
        return this.configuration.getString("oauth");
    }
}
