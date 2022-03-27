package net.kunmc.lab.teamkunpluginmanager.terminal.impl.player;

import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Progressbar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlayerProgressbar implements Progressbar
{
    private final Player player;
    private final BossBar bossBar;

    private int progressMax;
    private int progress;
    private String prefix;
    private String suffix;
    private int size;
    private boolean showing;
    private ProgressbarType type;

    public PlayerProgressbar(Player player, ProgressbarType type)
    {
        this.player = player;
        this.bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);

        this.type = type;
        this.progressMax = 100;
        this.progress = 0;
        this.prefix = null;
        this.suffix = null;
        this.size = 10;
        this.showing = false;

        this.bossBar.setVisible(false);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void setProgressMax(int max)
    {
        this.progressMax = max;
        this.update();
    }

    @Override
    public void setProgress(int progress)
    {
        this.progress = progress;
        this.update();
    }

    @Override
    public void setPrefix(@Nullable String prefix)
    {
        this.prefix = prefix;
        this.update();
    }

    @Override
    public void setSuffix(@Nullable String suffix)
    {
        this.suffix = suffix;
        this.update();
    }

    @Override
    public void setSize(int size)
    {
        this.size = size;
        this.update();
    }

    @Override
    public void show()
    {
        this.showing = true;
        if (this.type == ProgressbarType.BOSS_BAR)
            this.bossBar.setVisible(true);
        this.update();
    }

    @Override
    public void hide()
    {
        if (this.type == ProgressbarType.BOSS_BAR)
            this.bossBar.setVisible(false);
        this.showing = false;
    }

    @Override
    public void update()
    {
        if (!this.showing || this.progressMax == 0 || this.progress > this.progressMax)
            return;

        double percent = (double) this.progress / (double) this.progressMax;

        if (this.type == ProgressbarType.ACTION_BAR)
        {
            StringBuilder builder = new StringBuilder();
            if (this.prefix != null)
                builder.append(this.prefix);
            builder.append("[");
            for (int i = 0; i < this.size; i++)
            {
                if (i > progress * 10)
                    builder.append(ChatColor.RED).append("░");
                else
                    builder.append(ChatColor.GREEN).append("█");
            }
            builder.append(ChatColor.WHITE).append("]");

            ChatColor color;
            if (percent < 0.7)
                color = ChatColor.GREEN;
            else if (percent < 0.9)
                color = ChatColor.YELLOW;
            else
                color = ChatColor.RED;

            builder.append(color).append(this.progress).append(ChatColor.WHITE);
            if (this.suffix != null)
                builder.append(this.suffix);
            this.player.sendActionBar(Component.text(builder.toString()));
        }
        else if (this.type == ProgressbarType.BOSS_BAR)
        {
            this.bossBar.setProgress(percent);
            this.bossBar.setTitle((this.prefix == null ? "": this.prefix) +
                    ChatColor.WHITE +
                    "[ " + ChatColor.GREEN + this.progress +
                    ChatColor.WHITE + " / " +
                    ChatColor.GREEN + this.progressMax +
                    ChatColor.WHITE + " ]" + (this.suffix == null ? "": this.suffix));

            if (this.progress > 0.7)
                this.bossBar.setColor(BarColor.GREEN);
            else if (this.progress > 0.4)
                this.bossBar.setColor(BarColor.YELLOW);
            else
                this.bossBar.setColor(BarColor.RED);
        }
    }

    /**
     * プログレスバーのタイプを変更します
     *
     * @param type プログレスバーのタイプ
     */
    public void setType(ProgressbarType type)
    {
        this.hide();
        this.type = type;
        this.show();
    }

    enum ProgressbarType
    {
        BOSS_BAR,
        ACTION_BAR
    }
}
