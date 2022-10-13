package net.kunmc.lab.teamkunpluginmanager.installer.signals.assertion;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

/**
 * 指定されたプラグインが無視リストに登録されていることを示すシグナルです。
 * {@link IgnoredPluginSignal#setCancelInstall(boolean)} を変更することで、インストールのキャンセルを設定できます。
 */
@Data
public class IgnoredPluginSignal implements Signal
{
    @NotNull
    private final String pluginName;
    @NotNull
    private final PluginDescriptionFile pluginDescription;

    private boolean cancelInstall;

    public IgnoredPluginSignal(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.pluginDescription = pluginDescription;
        this.pluginName = pluginDescription.getName();

        this.cancelInstall = true;
    }
}
