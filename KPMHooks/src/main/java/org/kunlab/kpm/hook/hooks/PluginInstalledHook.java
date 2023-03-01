package org.kunlab.kpm.hook.hooks;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.hook.KPMHook;
import org.kunlab.kpm.meta.InstallOperator;

/**
 * プラグインがインストールされたときに呼び出されるフックです。
 */
@Data
@AllArgsConstructor
public class PluginInstalledHook implements KPMHook
{
    /**
     * プラグインのインストール者です。
     */
    @NotNull
    private final InstallOperator operator;
    /**
     * プラグインが依存関係であるかどうかを表します。
     */
    boolean isDependency;
    /**
     * プラグインのインストールに使用した解決クエリです。
     */
    @Nullable
    String resolveQuery;
}
