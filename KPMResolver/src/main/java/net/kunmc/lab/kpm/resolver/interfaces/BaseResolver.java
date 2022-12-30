package net.kunmc.lab.kpm.resolver.interfaces;

import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;

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

    /**
     * MultiResult から最初の要素を取得します。
     *
     * @param multiResult MultiResult
     * @return 最初の要素
     */
    default ResolveResult autoPickFirst(MultiResult multiResult, ResolveResult.Source source)
    {
        ResolveResult[] results = multiResult.getResults();

        if (results.length == 0)
            return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, source);

        ResolveResult result = results[0];
        if (result instanceof MultiResult)
            return this.autoPickFirst((MultiResult) result, source);

        return result;
    }
}
