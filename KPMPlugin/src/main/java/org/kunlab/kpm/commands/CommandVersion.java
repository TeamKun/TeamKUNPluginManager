package org.kunlab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.Notices;
import org.kunlab.kpm.TeamKunPluginManager;
import org.kunlab.kpm.interfaces.KPMRegistry;

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
        terminal.writeLine("KPM (TeamKUNPluginManager) v" + KPM_VERSION);
        terminal.writeLine("KPM Daemon v" + KPM_VERSION);  // NOTE: For any forks
        terminal.writeLine("PeyangPaperUtils v" + PYGLIB_VERSION);
        terminal.writeLine("");
    }

    private static void printLicenses(@NotNull Terminal terminal)
    {
        terminal.writeLine("Copyright (C) 2020-2021 TeamKUN., Peyang");
        terminal.write(TextComponent.fromLegacyText("The MIT License < https://opensource.org/licenses/MIT >"));
        terminal.writeLine("これはフリーソフトウェアです。ライセンスに従って再頒布および変更することができます。");
        terminal.writeLine("法律で認められている限り、著作者または著作権者は、**いかなる保証も行いません。**");
        terminal.write(TextComponent.fromLegacyText("バグ/新機能については " +
                "GitHub < https://github.com/TeamKUN/TeamKUNPluginManager > " +
                "から報告してください。"));
        terminal.writeLine("");
    }

    private static void printStatus(@NotNull Terminal terminal, @NotNull KPMRegistry registry)
    {
        terminal.writeLine("KPM は " + registry.getPluginMetaManager().getProvider().countPlugins() + " 個のプラグインを管理しています。");
        terminal.writeLine("そのうち " + Bukkit.getPluginManager().getPlugins().length + " 個のプラグインが有効です。");
        terminal.writeLine("");
        terminal.writeLine(registry.getAliasProvider().countAliases() + " 個のエイリアスが有効です。");
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
            terminal.error(mode + " は不明なモードです。");
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
        return of("KPM のバージョン/ステータス情報を表示します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("mode", String.join("|", AVAILABLE_MODES), "full")
        };
    }
}
