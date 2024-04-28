package org.kunlab.kpm.installer;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;

public class InstallResultImpl<P extends Enum<P>> implements InstallResult<P>
{
    private static final int MAX_RESULT_HOVER_ONE_LINE = 5;

    @Getter
    private final boolean success;

    @Getter
    private final InstallProgress<P, ? extends Installer<? extends InstallerArgument, ? extends Enum<?>, P>> progress;

    public InstallResultImpl(boolean success, InstallProgress<P, ? extends Installer<? extends InstallerArgument, ? extends Enum<?>, P>> progress)
    {
        progress.finish();
        this.success = success;
        this.progress = progress;
    }

    @Override
    public void printResultStatus(@NotNull Terminal terminal)
    {
        terminal.write(Text.empty()
                .append(Text.of("アップグレード：")
                        .append(getResultStatusComponent(this.getUpgraded(), "アップグレードされたプラグイン")))
                .append(Text.of("、インストール：")
                        .append(getResultStatusComponent(this.getInstalled(), "インストールされたプラグイン")))
                .append(Text.of("、削除：")
                        .append(getResultStatusComponent(this.getRemoved(), "削除されたプラグイン")))
                .append(Text.of("、保留：")
                        .append(getResultStatusComponent(this.getPending(), "保留されたプラグイン"))));

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

    private static Text getResultStatusComponent(@NotNull String[] components, @NotNull String hoverPrefix)
    {
        Text text = Text.empty();
        for (int i = 0; i < components.length; i++)
        {
            if (i % MAX_RESULT_HOVER_ONE_LINE == 0)
                text.append("\n");

            text.append("- " + components[i]).color(ChatColor.AQUA);
        }

        return text.append(" " + components.length + "個")
                .hoverText(Text.of(hoverPrefix + "\n").append(text));
    }
}
