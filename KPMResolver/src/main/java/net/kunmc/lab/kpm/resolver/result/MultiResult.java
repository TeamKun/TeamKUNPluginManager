package net.kunmc.lab.kpm.resolver.result;

import lombok.Value;
import net.kunmc.lab.kpm.resolver.interfaces.BaseResolver;

/**
 * 解決結果が複数ある場合に返されるクラスです。
 */
@Value
public class MultiResult implements ResolveResult
{
    /**
     * この解決を提供したリゾルバです。
     */
    BaseResolver resolver;
    /**
     * 解決結果の配列です。
     */
    ResolveResult[] results;
}
