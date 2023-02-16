package org.kunlab.kpm.interfaces.resolver;

import org.kunlab.kpm.interfaces.resolver.result.MultiResult;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;

/**
 * プラグインを解決するクラスです。
 */
public interface PluginResolver
{
    /**
     * リゾルバを追加します。
     *
     * @param resolver 追加するリゾルバ
     * @param names    リゾルバの名前とエイリアス
     */
    void addResolver(BaseResolver resolver, String... names);

    /**
     * フォールバックリゾルバを追加します。
     * フォールバックリゾルバは、プラグインが見つからなかった場合にフォールバックとして使用されるリゾルバです。
     *
     * @param resolver 追加するリゾルバ
     */
    void addFallbackResolver(BaseResolver resolver);

    /**
     * クエリを使用してプラグインを解決します。
     *
     * @param query クエリ
     */
    ResolveResult resolve(String query);

    /**
     * 複数の結果({@link MultiResult})を一つの結果にピックアップします。
     *
     * @param multiResult 複数の結果
     * @return 一つの結果
     */
    ResolveResult pickUpOne(MultiResult multiResult);
}
