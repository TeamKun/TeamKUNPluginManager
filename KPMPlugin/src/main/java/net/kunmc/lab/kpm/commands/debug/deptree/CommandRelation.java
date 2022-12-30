package net.kunmc.lab.kpm.commands.debug.deptree;

import com.google.common.base.Enums;
import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.meta.DependType;
import net.kunmc.lab.kpm.meta.DependencyNode;
import net.kunmc.lab.kpm.meta.PluginMeta;
import net.kunmc.lab.kpm.meta.PluginMetaProviderImpl;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommandRelation extends CommandBase
{
    private final PluginMetaProviderImpl provider;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String pluginName = args[0];
        if (!this.provider.isPluginMetaExists(pluginName))
        {
            terminal.error("Cannot find plugin meta of " + pluginName);
            return;
        }

        if (args.length < 2)
        {
            List<DependencyNode> dependencies = this.provider.getPluginMeta(pluginName, false, false).getDependsOn();
            terminal.success("Plugin " + pluginName + " depends on " +
                    dependencies.stream()
                            .map(DependencyNode::getDependsOn)
                            .sorted()
                            .collect(Collectors.joining(", ")));
            return;
        }

        Optional<DependType> type = Enums.getIfPresent(DependType.class, args[1].toUpperCase()).toJavaUtil();
        if (!type.isPresent())
        {
            terminal.error("Invalid relational type: " + args[1]);
            return;
        }

        PluginMeta meta = this.provider.getPluginMeta(pluginName, true, false);
        if (args.length < 3)
        {
            List<DependencyNode> dependencies = meta.getDependsOn();
            terminal.success("Plugin " + pluginName + " depends on " +
                    dependencies.stream()
                            .filter(d -> d.getDependType() == type.get())
                            .map(DependencyNode::getDependsOn)
                            .sorted()
                            .collect(Collectors.joining(", ")));
            return;
        }

        String targetPluginName = args[2];
        if (!this.provider.isPluginMetaExists(targetPluginName))
        {
            terminal.error("Cannot find plugin meta of " + targetPluginName);
            return;
        }


        DependencyNode node = meta.getDependsOn().stream()
                .filter(d -> d.getDependsOn().equals(targetPluginName))
                .findFirst()
                .orElse(null);

        boolean addMode = !meta.getDependsOn().removeIf(d -> d.getDependsOn().equals(targetPluginName));

        if (addMode)
        {
            meta.getDependsOn().add(new DependencyNode(pluginName, targetPluginName, type.get()));
            terminal.success("Added dependency " + targetPluginName + " to " + pluginName + " with type " + type.get());

            this.provider.savePluginMeta(meta);
        }
        else
        {
            assert node != null;
            terminal.success("Removed dependency " + targetPluginName + " from " + pluginName + " with type " + node.getDependType());

            this.provider.savePluginMeta(meta);
        }

        this.provider.deleteFromDependencyTree(pluginName);
        this.provider.deleteFromDependencyTree(targetPluginName);
        this.provider.buildDependencyTree(pluginName);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        switch (args.length)
        {
            case 1:
            case 3:
                return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                        .map(Plugin::getName)
                        .collect(Collectors.toList());
            case 2:
                return Arrays.stream(DependType.values()).parallel()
                        .map(DependType::name)
                        .collect(Collectors.toList());
            default:
                return null;
        }
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.debug.deptree.relation";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プラグインの依存関係を変更します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("plugin", "Plugin"),
                optional("depend_type", "DependType"),
                optional("depend_plugin", "Plugin")
        };
    }
}
