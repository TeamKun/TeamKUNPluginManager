package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;

/**
 * プラグイン解決結果のインタフェースです。
 */
public interface ResolveResult
{
    /**
     * この解決を提供したリゾルバです。
     *
     * @return リゾルバ
     */
    BaseResolver getResolver();

    /**
     * プラグイン供給元です。
     */
    @AllArgsConstructor
    enum Source
    {
        /**
         * GitHub
         */
        GITHUB("GitHub"),
        /**
         * @link https://www.spigotmc.org/resources/
         */
        SPIGOT_MC("Spigot-MC"),
        /**
         * @link https://dev.bukkit.org/projects/plugins/
         */
        DEV_BUKKIT("Bukkit-Project"),
        /**
         * @link https://curseforge.com/bukkit-plugins/
         */
        CURSE_FORGE("CurseForge"),
        /**
         * ローカルの既知プラグイン
         */
        LOCAL_KNOWN("Alias"),
        /**
         * 直リンク
         */
        DIRECT("Direct"),
        /**
         * 不明
         */
        UNKNOWN("Unknown");

        @Getter
        private final String name;

    }
}
