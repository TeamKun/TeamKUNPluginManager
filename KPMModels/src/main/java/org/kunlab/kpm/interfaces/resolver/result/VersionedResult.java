package org.kunlab.kpm.interfaces.resolver.result;

import java.util.List;

/**
 * プラグインの対応するバージョンを格納するインターフェースです。
 */
public interface VersionedResult
{
    /**
     * プラグインが対応するバージョン一覧です。
     */
    List<String> getVersions();
}