package net.kunmc.lab.teamkunpluginmanager.installer.exceptions;

/**
 * トークンが設定されていないことを表す例外です。
 */
public class TokenNotAvailableException extends IllegalStateException
{
    public TokenNotAvailableException(String s)
    {
        super(s);
    }

    public TokenNotAvailableException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TokenNotAvailableException(Throwable cause)
    {
        super(cause);
    }
}
