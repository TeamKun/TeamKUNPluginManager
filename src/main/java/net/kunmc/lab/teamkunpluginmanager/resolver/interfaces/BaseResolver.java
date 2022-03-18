package net.kunmc.lab.teamkunpluginmanager.resolver.interfaces;

import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;

/**
 * プラグインリゾルバのインターフェース
 */
public interface BaseResolver
{
    /**
     * クエリを解決します。
     *
     * @param query クエリ
     * @return クエリの解決結果
     */
    ResolveResult resolve(String query);

    /**
     * 複数のリソースを自動で一意に特定します。
     * @param multiResult リソースのリスト。
     * @return プラグイン。
     */
    ResolveResult autoPickOnePlugin(MultiResult multiResult);

    /**
     * 与えられたクエリが解決可能かどうかを返します。
     * @param query クエリ
     * @return クエリが解決可能ならばtrue
     */
    boolean isValidResolver(String query);
}
