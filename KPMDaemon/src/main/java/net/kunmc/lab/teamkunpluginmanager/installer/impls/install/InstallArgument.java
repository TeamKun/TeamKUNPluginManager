package net.kunmc.lab.teamkunpluginmanager.installer.impls.install;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstallerArgument;
import org.jetbrains.annotations.NotNull;

/**
 * インストールの引数を格納するクラスです。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InstallArgument extends AbstractInstallerArgument
{
    /**
     * インストールするプラグインのクエリ
     */
    @NotNull
    private final String query;
}
