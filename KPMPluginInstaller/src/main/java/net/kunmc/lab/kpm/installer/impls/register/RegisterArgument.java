package net.kunmc.lab.kpm.installer.impls.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import org.jetbrains.annotations.Nullable;

/**
 * トークン登録の引数を格納するクラスです。
 */
@Data
@AllArgsConstructor
public class RegisterArgument implements InstallerArgument
{
    /**
     * GitHubのアクセストークンです。
     */
    @Nullable
    private final String token;
}
