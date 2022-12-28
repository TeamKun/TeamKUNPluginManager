package net.kunmc.lab.plugin.kpmupgrader;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.KPMEnvironment;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.utils.versioning.Version;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Logger;

public class UpgradeImpl
{
    private static final String KPM_OWNER = "TeamKUN";
    private static final String KPM_NAME = "TeamKunPluginManager";

    private final KPMUpgraderPlugin plugin;
    private final Logger logger;
    private final Plugin currentKPM;
    private final Version currentKPMVersion;

    private final KPMDaemon daemon;

    public UpgradeImpl(KPMUpgraderPlugin plugin)
    {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        if ((this.currentKPM = plugin.getServer().getPluginManager().getPlugin(KPM_NAME)) == null)
        {
            this.logger.severe("KPM がこのサーバーにインストールされていません。");
            this.logger.info("KPMUpgrader は、 KPM による自動インストールでのみ使用できます。");
            this.destructSelf();
            this.currentKPMVersion = null;
            this.daemon = null;
            return;
        }

        Path dataDir = plugin.getDataFolder().toPath();
        Path kpmDataFolder = this.currentKPM.getDataFolder().toPath();
        this.daemon = new KPMDaemonMock(KPMEnvironment.builder(plugin)
                .tokenPath(kpmDataFolder.resolve("token.dat"))
                .tokenKeyPath(kpmDataFolder.resolve("token_key.dat"))
                .metadataDBPath(dataDir.resolve("plugins.db"))
                .aliasesDBPath(dataDir.resolve("aliases.db"))
                .organizations(Collections.emptyList())
                .sources(Collections.emptyMap())
                .build()
        );


        if ((this.currentKPMVersion = Version.ofNullable(this.currentKPM.getDescription().getVersion())) == null)
        {
            this.logger.severe("KPM のバージョンの取得に失敗しました。");
            this.destructSelf();
        }

    }

    private void destructSelf()
    {
        this.logger.info("お使いの KPM は、自動アッグレートに対応していないません。手動で KPM をアッグレートしてください。");
        Runner.run(() -> this.plugin.getServer().dispatchCommand(
                this.plugin.getServer().getConsoleSender(),
                "kpm upgrade-kpm destruct"
        ));
    }

    private SuccessResult resolveKPM(String version)
    {
        String query = "https://github.com/" + KPM_OWNER + "/" + KPM_NAME + "/releases/tag/" + version;

        ResolveResult resolveResult = this.daemon.getPluginResolver().resolve(query);

        if (resolveResult instanceof SuccessResult)
            return (SuccessResult) resolveResult;

        assert resolveResult instanceof ErrorResult;
        ErrorResult errorResult = (ErrorResult) resolveResult;

        this.logger.severe("KPM の取得に失敗しました：" + errorResult.getMessage());
        this.destructSelf();
        return null;
    }

    public void runUpgrade(String version)
    {
        this.logger.info("KPM をアッグレートしています ...");

        this.logger.info("令和最新版 KPM を解決しています。");
        SuccessResult result = this.resolveKPM(version);
        if (result == null)
            return;
        this.logger.info("KPM を解決しました：%s" + result.getVersion());
    }
}
