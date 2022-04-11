package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandClean extends CommandBase
{
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (DependencyTree.isErrors())
        {
            terminal.error("重大なエラーが検出されました。/kpm fix で修正を行ってください。");
            terminal.info("エラーが検出されたため、システムが保護されました。");
            return;
        }

        TeamKunPluginManager kpmInstance = TeamKunPluginManager.getPlugin();

        if (!kpmInstance.getSession().lock())
        {
            terminal.error("TeamKunPluginManagerが多重起動しています。");
            return;
        }

        terminal.info("依存関係ツリーを読み込み中...");

        String[] removable = Installer.getRemovableDataDirs();
        if (removable.length == 0)
        {
            terminal.info("削除可能な項目が見つかりませんでした。");
            kpmInstance.getSession().unlock();
            return;
        }

        String pluginName = null;
        if (args.length > 0 && !args[0].equals("all"))
            pluginName = args[0];

        if (pluginName != null)
            if (ArrayUtils.contains(removable, pluginName))
                removable = new String[]{pluginName};
            else
            {
                terminal.error("指定されたプラグインのデータフォルダが見つかりませんでした。");
                kpmInstance.getSession().unlock();
                return;
            }

        // TODO: aptっぽく、スキャンで引っかかったプラグインのリストを表示する

        terminal.writeLine(ChatColor.GREEN + "この操作で、以下の" + removable.length + "つのプラグインデータが削除されます: ");
        terminal.writeLine(ChatColor.AQUA + String.join(", ", removable));

        String[] finalRemovable = removable;
        Runner.run(() -> {
            QuestionResult result = terminal.getInput().
                    showQuestion("本当に続行しますか?", QuestionAttribute.YES, QuestionAttribute.CANCELLABLE)
                    .waitAndGetResult();

            if (result.test(QuestionAttribute.CANCELLABLE))
                terminal.error("キャンセルしました。");
            else if (result.test(QuestionAttribute.YES))
                removeDatas(terminal, finalRemovable);

            kpmInstance.getSession().unlock();
        }, (exception, bukkitTask) -> {
        });

    }

    private void removeDatas(Terminal terminal, String[] removables)
    {
        for (String removable : removables)
        {
            terminal.info(removable + " を削除しています...");
            Installer.clean(removable);
        }

        terminal.writeLine(Messages.getStatusMessage(0, removables.length, 0));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        List<String> list = new ArrayList<>(Collections.singleton("all"));

        if (args.length == 1)
            list.addAll(Arrays.asList(Installer.getRemovableDataDirs()));

        return list;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.clean";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("削除されて使用されなくなったプラグインのデータフォルダを再帰的に削除します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("name", "プラグイン名", "all"),
        };
    }
}
