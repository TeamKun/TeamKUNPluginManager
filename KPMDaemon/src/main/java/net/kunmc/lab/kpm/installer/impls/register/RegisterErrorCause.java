package net.kunmc.lab.kpm.installer.impls.register;

/**
 * トークン登録のエラーを表す列挙型です。
 */
public enum RegisterErrorCause
{
    /**
     * I/Oエラーが発生したことを示します。
     */
    IO_EXCEPTION_OCCURRED,
    /**
     * トークンが無効であることを示します。
     */
    INVALID_TOKEN,
    /**
     * キャンセルされたことを示します。
     */
    GENERATE_CANCELLED,
    /**
     * ユーザ検証コードの要求に失敗したことを示します。
     */
    VERIFICATION_CODE_REQUEST_FAILED,
    /**
     * コードの検証に失敗したことを示します。
     */
    VERIFICATION_FAILED,
}
