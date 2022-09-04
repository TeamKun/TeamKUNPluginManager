package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグインのインストール中であることを示すシグナルです。
 */
@Value
public class PluginInstallingSignal implements InstallerSignal
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
