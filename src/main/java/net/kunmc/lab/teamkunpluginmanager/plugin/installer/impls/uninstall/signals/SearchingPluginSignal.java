package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインを検索中であることを示すシグナルです。
 */
@Data
public class SearchingPluginSignal implements Signal
{
    @NotNull
    private String query;
}
