package net.kunmc.lab.teamkunpluginmanager.loader;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * プラグイン読込結果を表すクラスです。
 */
public enum PluginLoadResult
{
    /**
     * 正常
     */
    OK,

    // Errors
    /**
     * ファイルが存在しない
     */
    FILE_NOT_FOUND,
    /**
     * <code>plugin.yml</code> が不適切
     */
    INVALID_PLUGIN_DESCRIPTION,
    /**
     * プラグインのファイルが間違っている(メインクラスが存在しない等)
     */
    INVALID_PLUGIN_FILE,
    /**
     * プラグインの依存関係が読み込まれていない
     */
    DEPENDENCY_NOT_FOUND,
    /**
     * {@link Plugin#onLoad()} の実行中に例外が発生した
     */
    EXCEPTION_ON_ONLOAD_HANDLING,
    /**
     * その他のエラーでプラグインを有効にできなかった
     */
    ENABLE_PLUGIN_FAILED;

    @Getter
    private Exception exception; // implicitly final

    public PluginLoadResult withException(@NotNull Exception exception)
    {
        if (this.exception != null)
            this.exception = exception;
        return this;
    }
}
