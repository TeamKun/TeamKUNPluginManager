package org.kunlab.kpm.installer.impls.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;

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
