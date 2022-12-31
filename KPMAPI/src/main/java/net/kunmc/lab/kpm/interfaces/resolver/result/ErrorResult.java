package net.kunmc.lab.kpm.interfaces.resolver.result;

import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;
import net.kunmc.lab.kpm.resolver.ErrorCause;

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
