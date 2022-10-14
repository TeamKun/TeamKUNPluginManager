package net.kunmc.lab.teamkunpluginmanager.installer.impls.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstallerArgument;
import org.jetbrains.annotations.Nullable;

/**
 * トークン登録の引数を格納するクラスです。
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegisterArgument extends AbstractInstallerArgument
{
    /**
     * GitHubのアクセストークンです。
     */
    @Nullable
    private final String token;
}
