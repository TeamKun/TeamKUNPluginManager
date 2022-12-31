package net.kunmc.lab.kpm.resolver.result;

import lombok.Value;
import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;

/**
 * 解決結果が複数ある場合に返されるクラスです。
 */
@Value
public class MultiResultImpl implements MultiResult
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
