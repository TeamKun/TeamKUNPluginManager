package org.kunlab.kpm.interfaces.resolver.result;

import org.kunlab.kpm.interfaces.resolver.BaseResolver;
import org.kunlab.kpm.resolver.ErrorCause;

/**
 * 解決に失敗したことを表すクエリ解決結果です。
 */
public interface ErrorResult extends ResolveResult
{
    /**
     * この解決を提供したリゾルバです。
     */
    BaseResolver getResolver();

    /**
     * エラーのかんたんな理由です。
     */
    ErrorCause getCause();

    /**
     * プラグインの提供元です。
     */
    ResolveResult.Source getSource();

    /**
     * エラーの詳細な理由です。
     */
    String getMessage();
}
