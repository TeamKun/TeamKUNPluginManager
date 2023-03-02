package org.kunlab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.Notices;
import org.kunlab.kpm.TeamKunPluginManager;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@AllArgsConstructor
public class CommandVersion extends CommandBase
{
    private static final String[] AVAILABLE_MODES = {
            "full",
            "versions",
            "licenses",
            "status",
            "notice"
    };
    private static final String KPM_VERSION;
    private static final String PYGLIB_VERSION;

    static
    {

        // Read properties which called "version.properties" in the jar file.
        try (InputStream is = TeamKunPluginManager.getPlugin().getResource("versions.properties"))
        {
            Properties properties = new Properties();
            properties.load(is);
            KPM_VERSION = properties.getProperty("kpm");
            PYGLIB_VERSION = properties.getProperty("pyglib");
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }

    }

    private final KPMRegistry registry;

    private static void printInfo(Terminal terminal, KPMRegistry registry, String mode)
    {
        boolean isFull = mode.equals("full");

        if (isFull || mode.equals("versions"))
            printVersions(terminal);
        if (isFull || mode.equals("licenses"))
            printLicenses(terminal);
        if (isFull || mode.equals("status"))
            printStatus(terminal, registry);
        if (isFull || mode.equals("notice"))
            Notices.printAllNotice(registry, terminal);
    }

    private static void printVersions(@NotNull Terminal terminal)
    {
        terminal.writeLine(LangProvider.get(
                "command.version.versions.kpm",
                MsgArgs.of("version", KPM_VERSION)
        ));
        terminal.writeLine(LangProvider.get(
                "command.version.versions.daemon",
                MsgArgs.of("version", KPM_VERSION)
        ));

        terminal.writeLine(LangProvider.get(
                "command.version.versions.pyglib",
                MsgArgs.of("version", PYGLIB_VERSION)
        ));
        terminal.writeLine("");
    }

    private static void printLicenses(@NotNull Terminal terminal)
    {
        terminal.writeLine(LangProvider.get("command.version.license.1"));
        terminal.write(LangProvider.getComponent("command.version.license.2"));
        terminal.writeLine(LangProvider.get("command.version.license.3"));
        terminal.writeLine(LangProvider.get("command.version.license.4"));
        terminal.write(LangProvider.getComponent("command.version.license.bugs"));
        terminal.writeLine("");
    }

    private static void printStatus(@NotNull Terminal terminal, @NotNull KPMRegistry registry)
    {
        terminal.writeLine(LangProvider.get(
                "command.version.status.plugins",
                MsgArgs.of("plugins", registry.getPluginMetaManager().getProvider().countPlugins())
                        .add("enabled", Bukkit.getPluginManager().getPlugins().length)
        ));
        terminal.writeLine("");
        terminal.writeLine(LangProvider.get(
                "command.version.status.aliases",
                MsgArgs.of("aliases", registry.getAliasProvider().countAliases())
        ));
        terminal.writeLine("");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        String mode = "full";
        if (args.length > 0)
            mode = args[0];

        if (!ArrayUtils.contains(AVAILABLE_MODES, mode))
        {
            terminal.writeLine(LangProvider.get("command.version.invalid_mode"));
            return;
        }

        printInfo(terminal, this.registry, mode);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public String getPermission()
    {
        return "kpm.version";
    }

    @Override
    public net.kyori.adventure.text.TextComponent getHelpOneLine()
    {
        return LangProvider.getComponent("command.version");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("mode", String.join("|", AVAILABLE_MODES), "full")
        };
    }
}
