package net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

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
        private ArrayList<String> targetNamespaces;

        public Searching(@NotNull Plugin plugin, @NotNull String... targetNamespaces)
        {
            super(plugin);
            this.targetNamespaces = (ArrayList<String>) Arrays.asList(targetNamespaces);
        }
    }

    /**
     * レシピを削除する前にスローされるシグナルです。
     */
    @Getter
    public static class Removing extends PluginRegisteredRecipeSignal
    {
        /**
         * 削除するレシピです。
         */
        @NotNull
        private final Recipe recipe;

        public Removing(@NotNull Plugin plugin, @NotNull Recipe recipe)
        {
            super(plugin);
            this.recipe = recipe;
        }
    }
}
