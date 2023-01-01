package net.kunmc.lab.kpm.task.tasks.install.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグインのインストール中であることを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class PluginInstallingSignal extends Signal
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