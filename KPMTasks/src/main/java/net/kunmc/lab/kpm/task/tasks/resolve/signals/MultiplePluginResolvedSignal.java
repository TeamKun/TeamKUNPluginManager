package net.kunmc.lab.kpm.task.tasks.resolve.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 複数のプラグインが解決されたことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MultiplePluginResolvedSignal extends Signal
{
    /**
     * 解決する際に使用されたクエリです。
     */
    @NotNull
    private final String query;

    /**
     * 解決された複数のプラグインの結果です。
     */
    @NotNull
    private final MultiResult results;

    /**
     * <b>明示的に指定する</b>プラグインの解決結果です。
     * これに{@link net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult}を格納すると、使用するプラグインを一意に特定できます.
     * {@code null} の場合は、自動的に選択されます。
     */
    @Nullable
    private ResolveResult specifiedResult;

    /**
     * インストールをキャンセルするかどうかを示すフラグです。
     */
    private boolean cancel;
}