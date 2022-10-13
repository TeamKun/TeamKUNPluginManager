package net.kunmc.lab.teamkunpluginmanager.installer.exceptions;

/**
 * インストーラが既に実行中であることを表す例外です。
 */
public class InstallerRunningException extends IllegalStateException
{
    public InstallerRunningException(String s)
    {
        super(s);
    }

    public InstallerRunningException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InstallerRunningException(Throwable cause)
    {
        super(cause);
    }
}
