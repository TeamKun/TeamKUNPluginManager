package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class InstallResult
{
    private static final int MAX_RESULT_HOVER_ONE_LINE = 5;

    private final boolean success;
    private final InstallProgress progress;

    public static InstallResult success(InstallProgress progress)
    {
        progress.finish();
        return new InstallResult(true, progress);
    }

    public static InstallFailedInstallResult error(InstallProgress progress, FailedReason reason)
    {  // TODO: Implement debug mode
        progress.finish();
        return new InstallFailedInstallResult(progress, reason);
    }

    private static TextComponent getResultStatusComponent(@NotNull String[] components, @NotNull String hoverPrefix)
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < components.length; i++)
        {
            if (i % MAX_RESULT_HOVER_ONE_LINE == 0)
                builder.append("\n");

            builder.append(ChatColor.AQUA)
                    .append("- ")
                    .append(components[i]);
        }

        return Component.text(" " + components.length + "個")
                .hoverEvent(HoverEvent.showText(Component.text(hoverPrefix + "\n" + builder)));
    }

    public int getUpgradedCount()
    {
        return progress.getUpgraded().size();
    }

    public int getInstalledCount()
    {
        return progress.getInstalled().size();
    }

    public int getRemovedCount()
    {
        return progress.getRemoved().size();
    }

    public int getPendingCount()
    {
        return progress.getPending().size();
    }

    public String[] getUpgraded()
    {
        return progress.getUpgraded().toArray(new String[0]);
    }

    public String[] getInstalled()
    {
        return progress.getInstalled().toArray(new String[0]);
    }

    public String[] getRemoved()
    {
        return progress.getRemoved().toArray(new String[0]);
    }

    public String[] getPending()
    {
        return progress.getPending().toArray(new String[0]);
    }

    public void printResultStatus(@NotNull Terminal terminal)
    {
        TextComponent component = Component.text("")
                .append(Component.text("アップグレード：")
                        .append(getResultStatusComponent(getUpgraded(), "アップグレードされたプラグイン")))
                .append(Component.text("、インストール：")
                        .append(getResultStatusComponent(getInstalled(), "インストールされたプラグイン")))
                .append(Component.text("、削除：")
                        .append(getResultStatusComponent(getRemoved(), "削除されたプラグイン")))
                .append(Component.text("、保留：")
                        .append(getResultStatusComponent(getPending(), "保留されたプラグイン")));

        terminal.write(component);
    }
}
