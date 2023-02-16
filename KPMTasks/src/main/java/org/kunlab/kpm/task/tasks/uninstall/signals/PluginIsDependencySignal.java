package org.kunlab.kpm.task.tasks.uninstall.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.meta.DependencyNode;
import org.kunlab.kpm.signal.Signal;

import java.util.List;

/**
 * アンインストールしようとしたプラグインが、他のプラグインの依存関係にあることを示すシグナルです。
 * {@link PluginIsDependencySignal#setOperation(Operation)} を用いて、どのように処理するかを決定できます。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginIsDependencySignal extends Signal
{
    /**
     * 被依存関係を持つプラグインです。
     */
    @NotNull
    private final Plugin plugin;
    /**
     * 依存関係にあるプラグインのリストです。
     */
    @NotNull
    private final List<DependencyNode> dependedBy;

    /**
     * どのように依存関係を処理するかを設定します。
     */
    @Nullable
    private PluginIsDependencySignal.Operation operation;

    public PluginIsDependencySignal(@NotNull Plugin plugin, @NotNull List<DependencyNode> dependedBy)
    {
        this.plugin = plugin;
        this.dependedBy = dependedBy;
        this.operation = null;
    }

    /**
     * アンインストール時に実行する依存関係の処理方法です。
     */
    public enum Operation
    {
        /**
         * 依存関係にあるプラグインも全てアンインストールします。
         */
        UNINSTALL,
        /**
         * 依存関係にあるプラグインを無効化します。
         * サーバからはアンインストールされませんが, リロード時またはサーバ再起動時に有効化されなくなります。
         */
        DISABLE,
        /**
         * 依存関係にあるプラグインを無視してアンインストールします。
         * この場合、依存関係にあるプラグインがほぼ確実に動作しなくなります。
         */
        IGNORE,
        /**
         * アンロードのみ行い、サーバのプラグインフォルダからは削除しません。
         */
        UNLOAD_ONLY,
        /**
         * アンインストールをキャンセルします。
         */
        CANCEL
    }
}
