package org.kunlab.kpm.interfaces.hook;

import org.kunlab.kpm.interfaces.KPMRegistry;

/**
 * KPMフックを実行するクラスです。
 */
public interface HookExecutor
{
    /**
     * フックを実行します。
     *
     * @param recipient フックを受け取るクラス
     * @param hook      フック
     */
    void runHook(KPMHookRecipient recipient, KPMHook hook);

    /**
     * フックを実行します。
     *
     * @param recipients フックを受け取るクラスのリスト
     * @param hook       フック
     */
    void runHook(HookRecipientList recipients, KPMHook hook);

    KPMRegistry getRegistry();
}
