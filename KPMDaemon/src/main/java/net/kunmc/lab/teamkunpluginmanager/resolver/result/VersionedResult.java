package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import java.util.List;

/**
 * プラグインの対応するバージョンを格納するインターフェース。
 */
public interface VersionedResult
{
    /**
     * プラグインの対応する一覧。
     *
     */
    List<String> getVersions();
}
