package net.kunmc.lab.kpm.installer.impls.uninstall.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
