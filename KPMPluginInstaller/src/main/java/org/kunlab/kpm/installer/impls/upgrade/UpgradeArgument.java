package org.kunlab.kpm.installer.impls.upgrade;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.installer.InstallerArgument;

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
