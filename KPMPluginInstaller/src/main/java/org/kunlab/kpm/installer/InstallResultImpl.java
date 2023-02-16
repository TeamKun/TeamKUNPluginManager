package org.kunlab.kpm.installer;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.installer.InstallProgress;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.interfaces.installer.InstallerArgument;
import org.kunlab.kpm.interfaces.installer.PluginInstaller;

public class InstallResultImpl<P extends Enum<P>> implements InstallResult<P>
{
    private static final int MAX_RESULT_HOVER_ONE_LINE = 5;

    @Getter
    private final boolean success;

    @Getter
    private final InstallProgress<P, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, P>> progress;

    public InstallResultImpl(boolean success, InstallProgress<P, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, P>> progress)
    {
        progress.finish();
        this.success = success;
        this.progress = progress;
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

    @Override
    public int getUpgradedCount()
    {
        return this.progress.getUpgraded().size();
    }

    @Override
    public int getInstalledCount()
    {
        return this.progress.getInstalled().size();
    }

    @Override
    public int getRemovedCount()
    {
        return this.progress.getRemoved().size();
    }

    @Override
    public int getPendingCount()
    {
        return this.progress.getPending().size();
    }

    @Override
    public String[] getUpgraded()
    {
        return this.progress.getUpgraded().toArray(new String[0]);
    }

    @Override
    public String[] getInstalled()
    {
        return this.progress.getInstalled().toArray(new String[0]);
    }

    @Override
    public String[] getRemoved()
    {
        return this.progress.getRemoved().toArray(new String[0]);
    }

    @Override
    public String[] getPending()
    {
        return this.progress.getPending().toArray(new String[0]);
    }

    @Override
    public void printResultStatus(@NotNull Terminal terminal)
    {
        TextComponent component = Component.text("")
                .append(Component.text("アップグレード：")
                        .append(getResultStatusComponent(this.getUpgraded(), "アップグレードされたプラグイン")))
                .append(Component.text("、インストール：")
                        .append(getResultStatusComponent(this.getInstalled(), "インストールされたプラグイン")))
                .append(Component.text("、削除：")
                        .append(getResultStatusComponent(this.getRemoved(), "削除されたプラグイン")))
                .append(Component.text("、保留：")
                        .append(getResultStatusComponent(this.getPending(), "保留されたプラグイン")));

        terminal.write(component);
    }
}
