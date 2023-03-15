package org.kunlab.kpm.hook.mocks;

import org.kunlab.kpm.hook.HookListener;
import org.kunlab.kpm.hook.KPMHookRecipientBase;
import org.kunlab.kpm.hook.hooks.PluginInstalledHook;
import org.kunlab.kpm.hook.hooks.RecipesUnregisteringHook;
import org.kunlab.kpm.interfaces.KPMRegistry;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidHookRecipientMock extends KPMHookRecipientBase
{
    private static boolean isAnyHookCalled = false;

    public ValidHookRecipientMock(KPMRegistry registry)
    {
        super(registry);
    }

    public static void assertHookCalled()
    {
        assertTrue(isAnyHookCalled);

        isAnyHookCalled = false;
    }

    @HookListener
    public void onInstall(PluginInstalledHook hook)
    {
        isAnyHookCalled = true;
    }

    @HookListener
    public void onUninstall(PluginInstalledHook hook)
    {
        isAnyHookCalled = true;
    }

    @HookListener
    public void onRecipeUnregPre(RecipesUnregisteringHook.Pre hook)
    {
        isAnyHookCalled = true;
    }

    @HookListener
    public void onRecipeUnregPost(RecipesUnregisteringHook.Post hook)
    {
        isAnyHookCalled = true;
    }

    @HookListener
    public void onRecipeUnregSearch(RecipesUnregisteringHook.Searching hook)
    {
        isAnyHookCalled = true;
    }
}
