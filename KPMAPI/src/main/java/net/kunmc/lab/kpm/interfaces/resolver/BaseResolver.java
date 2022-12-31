package net.kunmc.lab.kpm.interfaces.resolver;

import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.QueryContext;

/**
 * プラグインの解決を行うクラスのインターフェースです。
 */
public interface BaseResolver
{
    /**
     * クエリを解決します。
     *
     * @param query クエリ
     * @return クエリの解決結果
     */
    ResolveResult resolve(QueryContext query);

    /**
     * 複数のリソースを自動で一意に特定します。
     *
     * @param multiResult リソースのリスト。
     * @return プラグイン。
     */
    ResolveResult autoPickOnePlugin(MultiResult multiResult);

    /**
     * 与えられたクエリがこのリゾルバで解決可能かどうかを返します。
     *
     * @param query クエリ
     * @return クエリが解決可能ならばtrue
     */
    boolean isValidResolver(QueryContext query);
}
