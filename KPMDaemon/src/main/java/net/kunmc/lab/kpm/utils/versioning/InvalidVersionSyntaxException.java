package net.kunmc.lab.kpm.utils.versioning;

/**
 * バージョンの構文が不正な場合に投げられる例外です。
 */
public class InvalidVersionSyntaxException extends IllegalArgumentException
{
    public InvalidVersionSyntaxException(String message)
    {
        super(message);
    }
}
