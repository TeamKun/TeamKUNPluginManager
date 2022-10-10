package net.kunmc.lab.teamkunpluginmanager.installer.impls.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstallerArgument;

import java.util.HashMap;

/**
 * エイリアスのアップデートの引数を格納するクラスです。
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateArgument extends AbstractInstallerArgument
{
    /**
     * エイリアスのソースのURLです。
     */
    HashMap<String, String> remotes;  // TODO: Migrate to Map<String, String> remotes;
}
