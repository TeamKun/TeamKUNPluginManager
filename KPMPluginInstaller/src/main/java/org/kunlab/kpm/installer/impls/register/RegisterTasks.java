package org.kunlab.kpm.installer.impls.register;

/**
 * トークン登録のタスクを表す列挙型です。
 */
public enum RegisterTasks
{
    /**
     * トークンの登録が初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * トークンの登録中であることを示します。
     */
    REGISTERING_TOKEN,
    /**
     * ユーザ検証コードの要求中であることを示します。
     */
    REQUESTING_USER_VERIFICATION_CODE,
    /**
     * ユーザの検証コードの入力を待機していることを示します。
     */
    POLLING_USER_VERIFICATION,
}
