package net.kunmc.lab.kpm;

import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;

/**
 * KPM のモジュールのレジストリです。
 */
public interface KPMRegistry
{
    /**
     * エイリアスを管理するクラスです。
     */
    AliasProvider getAliasProvider();
}
