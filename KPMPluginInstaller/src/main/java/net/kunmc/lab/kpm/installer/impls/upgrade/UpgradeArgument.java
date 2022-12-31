package net.kunmc.lab.kpm.installer.impls.upgrade;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * アップグレードの引数を格納するクラスです。
 */
@Data
@AllArgsConstructor
public class UpgradeArgument implements InstallerArgument
{
    /**
     * アップグレード対象のプラグインの名前のリストです。
     * これをnullにすると、全てのプラグインをアップグレードします。
     */
    @Nullable
    private List<String> targetPlugins;
}
