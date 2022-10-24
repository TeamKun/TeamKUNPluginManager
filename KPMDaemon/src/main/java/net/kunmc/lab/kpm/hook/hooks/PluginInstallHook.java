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
public class PluginInstallHook implements KPMHook
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

    public static class Pre extends PluginInstallHook
    {
        public Pre(@NotNull InstallOperator operator, boolean isDependency, @Nullable String resolveQuery)
        {
            super(operator, isDependency, resolveQuery);
        }

        public Pre(@NotNull InstallOperator operator, boolean isDependency)
        {
            super(operator, isDependency, null);
        }

        public Pre(@NotNull InstallOperator operator)
        {
            super(operator, false, null);
        }
    }

    public static class Post extends PluginInstallHook
    {
        public Post(@NotNull InstallOperator operator, boolean isDependency, @Nullable String resolveQuery)
        {
            super(operator, isDependency, resolveQuery);
        }

        public Post(@NotNull InstallOperator operator, boolean isDependency)
        {
            super(operator, isDependency, null);
        }

        public Post(@NotNull InstallOperator operator)
        {
            super(operator, false, null);
        }
    }
}
