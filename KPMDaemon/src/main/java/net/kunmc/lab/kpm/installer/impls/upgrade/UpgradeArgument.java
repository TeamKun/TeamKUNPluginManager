package net.kunmc.lab.kpm.installer.impls.upgrade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.installer.AbstractInstallerArgument;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * アップグレードの引数を格納するクラスです。
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpgradeArgument extends AbstractInstallerArgument
{
    /**
     * アップグレード対象のプラグインの名前のリストです。
     * これをnullにすると、全てのプラグインをアップグレードします。
     */
    @Nullable
    private List<String> targetPlugins;
}
