package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * プラグインが登録したレシピについてのシグナルです。
 */
@Data
public class PluginRegisteredRecipeSignal implements Signal
{
    /**
     * レシピを登録したプラグインです。
     */
    @NotNull
    private final Plugin plugin;

    /**
     * 登録されたレシピが1つ見つかった場合にスローされるシグナルです。
     */
    @Getter
    @Setter
    public static class FoundOne extends PluginRegisteredRecipeSignal
    {
        /**
         * 使用されたシグニチャです。
         */
        @NotNull
        private final String signatureNamespace;
        /**
         * 見つかったレシピです。
         */
        @NotNull
        private final Recipe recipe;

        /**
         * 登録解除するかどうかを決めるフラグです。
         * デフォルトでは {@code true} です。
         */
        private boolean doUnregister;

        public FoundOne(@NotNull Plugin plugin, @NotNull String signatureNamespace, @NotNull Recipe recipe)
        {
            super(plugin);
            this.signatureNamespace = signatureNamespace;
            this.recipe = recipe;

            this.doUnregister = true;
        }
    }

    /**
     * プラグインのレシピを検索する前にスローされるシグナルです。
     */
    @Getter
    @Setter
    public static class Searching extends PluginRegisteredRecipeSignal
    {
        /**
         * 使用するシグニチャです。
         * デフォルトでは, プラグインの名前(Lower case)が使用されます。
         */
        @NotNull
        private String[] targetNamespaces;

        public Searching(@NotNull Plugin plugin)
        {
            super(plugin);

            this.targetNamespaces = new String[]{plugin.getName().toLowerCase(Locale.ROOT)};
        }
    }
}
