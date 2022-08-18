package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Data
public class PluginRegisteredRecipeSignal implements InstallerSignal
{
    @NotNull
    private final Plugin plugin;

    @Getter
    @Setter
    public static class FoundOne extends PluginRegisteredRecipeSignal
    {
        @NotNull
        private final String signatureNamespace;
        @NotNull
        private final Recipe recipe;

        private boolean doUnregister;

        public FoundOne(@NotNull Plugin plugin, @NotNull String signatureNamespace, @NotNull Recipe recipe)
        {
            super(plugin);
            this.signatureNamespace = signatureNamespace;
            this.recipe = recipe;

            this.doUnregister = true;
        }
    }

    @Getter
    @Setter
    public static class Searching extends PluginRegisteredRecipeSignal
    {
        @NotNull
        private String[] targetNamespaces;

        public Searching(@NotNull Plugin plugin)
        {
            super(plugin);

            this.targetNamespaces = new String[]{plugin.getName().toLowerCase(Locale.ROOT)};
        }
    }
}
