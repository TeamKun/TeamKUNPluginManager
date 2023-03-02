package org.kunlab.kpm.installer.impls.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;

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
