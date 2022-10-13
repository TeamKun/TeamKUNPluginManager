package net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstallerArgument;
import org.jetbrains.annotations.NotNull;

/**
 * アンインストールの引数を格納するクラスです。
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UninstallArgument extends AbstractInstallerArgument
{
    /**
     * アンインストールするプラグインの名前
     */
    @NotNull
    private final String[] plugins;

    /**
     * 単一のプラグインをアンインストールするための引数を生成します。
     *
     * @param plugin アンインストールするプラグイン
     */
    public UninstallArgument(@NotNull String plugin)
    {
        this(new String[]{plugin});
    }
}
