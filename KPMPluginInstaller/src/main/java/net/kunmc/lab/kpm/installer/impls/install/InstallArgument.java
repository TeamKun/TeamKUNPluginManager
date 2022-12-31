package net.kunmc.lab.kpm.installer.impls.install;

import lombok.Builder;
import lombok.Data;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.resolver.result.AbstractSuccessResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * インストールの引数を格納するクラスです。
 */
@Data
@Builder
public class InstallArgument implements InstallerArgument
{
    /**
     * インストールするプラグインのクエリです。
     * {@link #resolveResult} またはこのフィールドのどちらかが指定されている必要があります。
     */
    @Nullable
    private final String query;
    /**
     * インストールするプラグインの解決結果です。
     * {@link #query} またはこのフィールドのどちらかが指定されている必要があります。
     */
    @Nullable
    private final SuccessResult resolveResult;

    /**
     * 除外チェックをスキップするかどうかのフラグです。
     */
    @Builder.Default
    private final boolean skipExcludeChecks = false;

    /**
     * 同じプラグインが既にインストールされている場合に置換するかどうかのフラグです。
     * このフラグが true の場合, シグナルの選択は無視して置換されます。
     */
    @Builder.Default
    private final boolean replaceOldPlugin = false;

    /**
     * 強制インストールを行うかどうかのフラグです。
     * デフォルトでは, 状況に応じたシグナルが送信され, 選択を促されます。
     * このフラグが true の場合, シグナルは送信されますが, シグナルの選択は無視され, インストールが行われます。
     */
    @Builder.Default
    private final boolean forceInstall = false;

    /**
     * プラグインの配置のみのモードです。
     * このフラグを {@code true} にした場合, プラグインの読み込みは行われません。
     */
    @Builder.Default
    private final boolean onyLocate = false;

    public static InstallArgumentBuilder builder(@NotNull String query)
    {
        return new InstallArgumentBuilder().query(query);
    }

    public static InstallArgumentBuilder builder(@NotNull AbstractSuccessResult result)
    {
        return new InstallArgumentBuilder().resolveResult(result);
    }
}

