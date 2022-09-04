package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 複数のプラグインが解決されたことを示すシグナルです。
 */
@Data
public class MultiplePluginResolvedSignal implements InstallerSignal
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
     * これに{@link net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult}を格納すると、使用するプラグインを一意に特定できます.
     * {@code null} の場合は、自動的に選択されます。
     */
    @Nullable
    private ResolveResult specifiedResult;
}
