package net.kunmc.lab.kpm.hook.hooks;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.kpm.hook.KPMHook;
import net.kunmc.lab.kpm.meta.InstallOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
