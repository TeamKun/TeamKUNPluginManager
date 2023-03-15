package org.kunlab.kpm.hook.mocks;

import org.kunlab.kpm.hook.HookListener;
import org.kunlab.kpm.hook.KPMHookRecipientBase;
import org.kunlab.kpm.hook.hooks.PluginInstalledHook;
import org.kunlab.kpm.hook.hooks.RecipesUnregisteringHook;
import org.kunlab.kpm.interfaces.KPMRegistry;

public class InvalidHookRecipientMock
{
    public static class NoBaseInherit
    {
        @HookListener
        public void onInstall(PluginInstalledHook hook)
        {

        }

        @HookListener
        public void onUninstall(PluginInstalledHook hook)
        {

        }

        @HookListener
        public void onRecipeUnregPre(RecipesUnregisteringHook.Pre hook)
        {

        }

        @HookListener
        public void onRecipeUnregPost(RecipesUnregisteringHook.Post hook)
        {

        }

        @HookListener
        public void onRecipeUnregSearch(RecipesUnregisteringHook.Searching hook)
        {

        }
    }

    public static class NoValidConstructor extends KPMHookRecipientBase
    {
        public NoValidConstructor(Void v)
        {
            super(null);
        }

        @HookListener
        public void onInstall(PluginInstalledHook hook)
        {

        }

        @HookListener
        public void onUninstall(PluginInstalledHook hook)
        {

        }

        @HookListener
        public void onRecipeUnregPre(RecipesUnregisteringHook.Pre hook)
        {

        }

        @HookListener
        public void onRecipeUnregPost(RecipesUnregisteringHook.Post hook)
        {

        }

        @HookListener
        public void onRecipeUnregSearch(RecipesUnregisteringHook.Searching hook)
        {

        }
    }

    public static class CannotCreateInstance extends KPMHookRecipientBase
    {
        private CannotCreateInstance(KPMRegistry registry)
        {
            super(registry);
        }

        @HookListener
        public void onInstall(PluginInstalledHook hook)
        {

        }

        @HookListener
        public void onUninstall(PluginInstalledHook hook)
        {

        }

        @HookListener
        public void onRecipeUnregPre(RecipesUnregisteringHook.Pre hook)
        {

        }

        @HookListener
        public void onRecipeUnregPost(RecipesUnregisteringHook.Post hook)
        {

        }

        @HookListener
        public void onRecipeUnregSearch(RecipesUnregisteringHook.Searching hook)
        {

        }
    }

}
