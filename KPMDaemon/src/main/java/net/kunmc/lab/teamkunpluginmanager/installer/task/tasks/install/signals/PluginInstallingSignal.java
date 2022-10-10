package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグインのインストール中であることを示すシグナルです。
 */
@Value
public class PluginInstallingSignal implements Signal
{
    /**
     * 対象のプラグインのパスです。
     */
    @NotNull
    Path path;
    /**
     * 対象のプラグインの プラグイン情報ファイルです。
     */
    @NotNull
    PluginDescriptionFile pluginDescription;
}
