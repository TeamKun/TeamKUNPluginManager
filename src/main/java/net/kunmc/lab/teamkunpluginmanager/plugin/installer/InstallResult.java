package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * インストールの結果を表します。
 *
 * @param <P> インストールの進捗状況の型
 */
public class InstallResult<P extends Enum<P>>
{
    private static final int MAX_RESULT_HOVER_ONE_LINE = 5;

    /**
     * インストールが成功したかどうかです。
     */
    @Getter
    private final boolean success;
    /**
     * インストールの進捗状況です。
     */
    @Getter
    private final InstallProgress<P> progress;

    public InstallResult(boolean success, InstallProgress<P> progress)
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

    /**
     * アップグレードされたプラグインの数を取得します。
     *
     * @return アップグレードされたプラグインの数
     */
    public int getUpgradedCount()
    {
        return progress.getUpgraded().size();
    }

    /**
     * インストールされたプラグインの数を取得します。
     *
     * @return インストールされたプラグインの数
     */
    public int getInstalledCount()
    {
        return progress.getInstalled().size();
    }

    /**
     * 削除されたプラグインの数を取得します。
     *
     * @return 削除されたプラグインの数
     */
    public int getRemovedCount()
    {
        return progress.getRemoved().size();
    }

    /**
     * 保留中としてマークされたプラグインの数を取得します。
     *
     * @return 保留中としてマークされたプラグインの数
     */
    public int getPendingCount()
    {
        return progress.getPending().size();
    }

    /**
     * アップグレードされたプラグインの名前を取得します。
     *
     * @return アップグレードされたプラグインの名前
     */
    public String[] getUpgraded()
    {
        return progress.getUpgraded().toArray(new String[0]);
    }

    /**
     * インストールされたプラグインの名前を取得します。
     *
     * @return インストールされたプラグインの名前
     */
    public String[] getInstalled()
    {
        return progress.getInstalled().toArray(new String[0]);
    }

    /**
     * 削除されたプラグインの名前を取得します。
     *
     * @return 削除されたプラグインの名前
     */
    public String[] getRemoved()
    {
        return progress.getRemoved().toArray(new String[0]);
    }

    /**
     * 保留中としてマークされたプラグインの名前を取得します。
     *
     * @return 保留中としてマークされたプラグインの名前
     */
    public String[] getPending()
    {
        return progress.getPending().toArray(new String[0]);
    }

    /**
     * インストールの結果をコンソールに出力します。
     *
     * @param terminal 出力先のターミナル
     */
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
