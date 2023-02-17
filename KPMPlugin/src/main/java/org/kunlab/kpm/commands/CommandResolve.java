package org.kunlab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;
import org.kunlab.kpm.lang.LangProvider;

import java.util.List;

@AllArgsConstructor
public class CommandResolve extends CommandBase
{
    private final KPMRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1, 1))
            return;

        String query = args[0];

        terminal.info(LangProvider.get("command.resolve.resolving"));
        ResolveResult result = this.registry.getPluginResolver().resolve(query);

        ResolveResultWriter writer = new ResolveResultWriter(terminal, result);
        writer.write();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.resolve";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return LangProvider.getComponent("command.resolve");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("query", "string")
        };
    }
}
