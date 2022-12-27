package net.kunmc.lab.plugin.kpmupgrader;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.KPMEnvironment;
import net.kunmc.lab.kpm.utils.versioning.Version;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Collections;

public final class KPMUpgraderPlugin extends JavaPlugin
{
    private static final String KPM_NAME = "TeamKunPluginManager";

    private KPMDaemon kpmDaemon;
    private Version currentKPMVersion;

    private void checkKPMEnabled()
    {
        if (this.getServer().getPluginManager().getPlugin(KPM_NAME) == null)
        {
            this.getLogger().severe("KPM がこのサーバーにインストールされていません。");
            this.getLogger().info("KPMUpgrader は、 KPM による自動インストールでのみ使用できます。");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable()
    {
        this.checkKPMEnabled();

        Plugin kpm = this.getServer().getPluginManager().getPlugin(KPM_NAME);
        assert kpm != null;

        Path dataDir = this.getDataFolder().toPath();
        Path kpmDataFolder = kpm.getDataFolder().toPath();

        this.kpmDaemon = new KPMDaemon(
                KPMEnvironment.builder(this)
                        .tokenPath(kpmDataFolder.resolve("token.dat"))
                        .tokenKeyPath(kpmDataFolder.resolve("token_key.dat"))
                        .metadataDBPath(dataDir.resolve("plugins.db"))
                        .aliasesDBPath(dataDir.resolve("aliases.db"))
                        .organizations(Collections.emptyList())
                        .sources(Collections.emptyMap())
                        .build()
        );

    }
}
