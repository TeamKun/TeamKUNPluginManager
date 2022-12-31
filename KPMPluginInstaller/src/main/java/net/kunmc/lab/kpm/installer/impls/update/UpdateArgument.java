package net.kunmc.lab.kpm.installer.impls.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;

import java.util.Map;

/**
 * エイリアスのアップデートの引数を格納するクラスです。
 */
@Data
@AllArgsConstructor
public class UpdateArgument implements InstallerArgument
{
    /**
     * エイリアスのソースのURLです。
     */
    Map<String, String> remotes;
}