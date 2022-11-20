package net.kunmc.lab.kpm.installer.impls.install;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.installer.AbstractInstallerArgument;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * インストールの引数を格納するクラスです。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class InstallArgument extends AbstractInstallerArgument
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

    public static InstallArgumentBuilder builder(@NotNull String query)
    {
        return new InstallArgumentBuilder().query(query);
    }

    public static InstallArgumentBuilder builder(@NotNull SuccessResult result)
    {
        return new InstallArgumentBuilder().resolveResult(result);
    }
}

