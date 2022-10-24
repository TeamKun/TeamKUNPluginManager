package net.kunmc.lab.kpm.hook;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * KPMフックの受け取りを行うクラスを管理するクラスです。
 */
public class HookRecipientList extends ArrayList<KPMHookRecipient>
{
    @Getter
    private final HookExecutor executor;

    public HookRecipientList(@NotNull HookExecutor executor)
    {
        this.executor = executor;
    }

    /**
     * フックを実行します。
     *
     * @param hook フック
     */
    public void runHook(KPMHook hook)
    {
        for (KPMHookRecipient recipient : this)
            this.executor.runHook(recipient, hook);
    }
}
