package net.kunmc.lab.kpm.hook;

import lombok.Getter;
import net.kunmc.lab.kpm.KPMDaemon;

import java.lang.reflect.Method;
import java.util.List;

/**
 * KPMフックを実行するクラスです。
 */
public class HookExecutor
{
    @Getter
    private final KPMDaemon daemon;

    public HookExecutor(KPMDaemon daemon)
    {
        this.daemon = daemon;
    }

    /**
     * フックを実行します。
     *
     * @param recipient フックを受け取るクラス
     * @param hook      フック
     */
    public void runHook(KPMHookRecipient recipient, KPMHook hook)
    {
        Method hookMethod = recipient.getHookListener(hook.getClass());

        if (hookMethod == null)
            throw new IllegalStateException("Hook method not found: " + hook.getClass().getName());

        try
        {
            hookMethod.invoke(recipient, hook);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * フックを実行します。
     *
     * @param recipients フックを受け取るクラスのリスト
     * @param hook       フック
     */
    public void runHook(List<KPMHookRecipient> recipients, KPMHook hook)
    {
        for (KPMHookRecipient recipient : recipients)
            this.runHook(recipient, hook);
    }
}
