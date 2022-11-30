package net.kunmc.lab.kpm.installer.task.tasks.install.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグインの読み込み中であることを示すシグナルです。
 * {@link org.bukkit.plugin.PluginManager#loadPlugin(java.io.File)} の呼び出しの前後にスローされます。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginLoadSignal extends Signal
{
    /**
     * 対象のプラグインのパスです。
     */
    @NotNull
    private final Path pluginPath;
    /**
     * 対象のプラグインのプラグイン情報ファイルです。
     */
    @NotNull
    private final PluginDescriptionFile pluginDescription;

    /**
     * 読み込みを行う前に送信されるシグナルです。
     * 注意：読み込みを行う前なので、 {@link Plugin} の取得はできません。
     */
    public static class Pre extends PluginLoadSignal
    {
        public Pre(@NotNull Path pluginPath, @NotNull PluginDescriptionFile pluginDescription)
        {
            super(pluginPath, pluginDescription);
        }
    }

    /**
     * 読み込みを行った後に送信されるシグナルです。
     */
    public static class Post extends PluginLoadSignal
    {
        /**
         * 読み込まれたプラグインです。
         */
        @NotNull
        @Getter
        private final Plugin plugin;

        public Post(@NotNull Path pluginPath, @NotNull PluginDescriptionFile pluginDescription, @NotNull Plugin plugin)
        {
            super(pluginPath, pluginDescription);
            this.plugin = plugin;
        }
    }
}
