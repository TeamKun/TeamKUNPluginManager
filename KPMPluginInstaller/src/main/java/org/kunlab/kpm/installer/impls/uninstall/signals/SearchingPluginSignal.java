package org.kunlab.kpm.installer.impls.uninstall.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインを検索中であることを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SearchingPluginSignal extends Signal
{
    @NotNull
    private String query;
}
