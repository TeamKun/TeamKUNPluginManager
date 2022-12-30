package net.kunmc.lab.kpm.interfaces.hook;

import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.kpm.hook.KPMHookRecipient;

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
