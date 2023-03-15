package org.kunlab.kpm.hook;

import org.junit.jupiter.api.Test;
import org.kunlab.kpm.hook.interfaces.HookExecutor;
import org.kunlab.kpm.hook.mocks.RandomHookMock;
import org.kunlab.kpm.hook.mocks.ValidHookRecipientMock;
import org.kunlab.kpm.interfaces.KPMRegistry;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookExecutorTest
{
    private static KPMRegistry getMockedRegistry()
    {
        KPMRegistry registry = mock(KPMRegistry.class);
        Logger logger = mock(Logger.class);
        when(registry.getLogger()).thenReturn(logger);

        return registry;
    }

    @Test
    public void ハンドラが存在しないフック実行に失敗するか()
    {
        HookExecutor executor = new HookExecutorImpl(getMockedRegistry());
        assertThrows(
                IllegalStateException.class,
                () -> executor.runHook(
                        new ValidHookRecipientMock(getMockedRegistry()),
                        new RandomHookMock()
                )
        );
    }

    // フックが実行されるかは, HookExecutorTestで網羅されている
}
