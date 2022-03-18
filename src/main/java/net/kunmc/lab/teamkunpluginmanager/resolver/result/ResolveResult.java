package net.kunmc.lab.teamkunpluginmanager.resolver.result;

/**
 * プラグイン解決結果のインタフェース
 */
public interface ResolveResult
{

    /**
     * プラグイン供給元
     */
    enum Source
    {
        /**
         * GitHub
         */
        GITHUB,
        /**
         * @link https://www.spigotmc.org/resources/
         */
        SPIGOT_MC,
        /**
         * @link https://dev.bukkit.org/projects/plugins/
         */
        DEV_BUKKIT,
        /**
         * @link https://curseforge.com/bukkit-plugins/
         */
        CURSE_FORGE,
        /**
         * 直リンク
         */
        DIRECT
    }
}
