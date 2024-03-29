package org.kunlab.kpm.installer.signals.assertion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * 指定されたプラグインが無視リストに登録されていることを示すシグナルです。
 * {@link IgnoredPluginSignal#setContinueInstall(boolean)} を変更することで、インストールのキャンセルを設定できます。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class IgnoredPluginSignal extends Signal
{
    @NotNull
    private final String pluginName;
    @NotNull
    private final PluginDescriptionFile pluginDescription;

    private boolean continueInstall;

    public IgnoredPluginSignal(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.pluginDescription = pluginDescription;
        this.pluginName = pluginDescription.getName();

        this.continueInstall = false;
    }
}
