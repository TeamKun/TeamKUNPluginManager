package org.kunlab.kpm.hook;

import org.junit.jupiter.api.Test;
import org.kunlab.kpm.hook.hooks.PluginInstalledHook;
import org.kunlab.kpm.hook.hooks.RecipesUnregisteringHook;
import org.kunlab.kpm.hook.interfaces.HookExecutor;
import org.kunlab.kpm.hook.interfaces.HookRecipientList;
import org.kunlab.kpm.hook.mocks.InvalidHookRecipientMock;
import org.kunlab.kpm.hook.mocks.ValidHookRecipientMock;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.InvalidInformationFileException;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookRecipientListTest
{
    private static KPMRegistry getMockedRegistry()
    {
        KPMRegistry registry = mock(KPMRegistry.class);
        Logger logger = mock(Logger.class);
        when(registry.getLogger()).thenReturn(logger);

        return registry;
    }

    private static void tryRegisterInvalidHookRecipient(Class<?> clazz)
    {
        KPMRegistry reg = getMockedRegistry();

        HookRecipientList list = new HookRecipientListImpl(reg, mock(HookExecutor.class));
        list.add(clazz.getName());

        assertThrows(InvalidInformationFileException.class, () -> list.bakeHooks(reg));
    }

    @Test
    void 正常なフッククラスを登録できるか()
    {
        KPMRegistry reg = getMockedRegistry();

        HookRecipientList list = new HookRecipientListImpl(reg, mock(HookExecutor.class));
        list.add(ValidHookRecipientMock.class.getName());

        assertDoesNotThrow(() -> list.bakeHooks(reg));
    }

    @Test
    void 存在しないクラス名の登録が失敗するか()
    {
        KPMRegistry reg = getMockedRegistry();

        HookRecipientList list = new HookRecipientListImpl(reg, mock(HookExecutor.class));
        list.add("org.kunlab.kpm.hook.mocks.ThisClassDoesNotExist");

        assertThrows(InvalidInformationFileException.class, () -> list.bakeHooks(reg));
    }

    @Test
    void Base継承なしのクラスの登録に失敗するか()
    {
        tryRegisterInvalidHookRecipient(InvalidHookRecipientMock.NoBaseInherit.class);
    }

    @Test
    void 正しいコンストラクタなしのクラスの登録に失敗するか()
    {
        // KPMRegistry を1つのみ受け取るコンストラクタが正

        tryRegisterInvalidHookRecipient(InvalidHookRecipientMock.NoValidConstructor.class);
        tryRegisterInvalidHookRecipient(InvalidHookRecipientMock.CannotCreateInstance.class);
    }

    @Test
    void フックを実行できるか()
    {
        KPMRegistry reg = getMockedRegistry();

        HookRecipientList list = new HookRecipientListImpl(reg, new HookExecutorImpl(reg));
        list.add(ValidHookRecipientMock.class.getName());


        assertDoesNotThrow(() -> list.bakeHooks(reg));

        assertDoesNotThrow(() -> list.runHook(mock(PluginInstalledHook.class)));
        ValidHookRecipientMock.assertHookCalled();
        assertDoesNotThrow(() -> list.runHook(mock(RecipesUnregisteringHook.Pre.class)));
        ValidHookRecipientMock.assertHookCalled();  // インナークラスもテスト
    }
}
