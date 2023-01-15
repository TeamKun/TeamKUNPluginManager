package net.kunmc.lab.kpm.interfaces.resolver.result;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;

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
        GITHUB,
        /**
         * <a href="https://www.spigotmc.org/resources/">...</a>
         */
        SPIGOT_MC,
        /**
         * <a href="https://dev.bukkit.org/projects/plugins/">...</a>
         */
        DEV_BUKKIT,
        /**
         * <a href="https://curseforge.com/bukkit-plugins/">...</a>
         */
        CURSE_FORGE,
        /**
         * ローカルの既知プラグイン
         */
        LOCAL_KNOWN,
        /**
         * 直リンク
         */
        DIRECT,
        /**
         * 不明
         */
        UNKNOWN
    }
}
