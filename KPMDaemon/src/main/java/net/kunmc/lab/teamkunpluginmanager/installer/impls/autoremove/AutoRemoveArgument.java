package net.kunmc.lab.teamkunpluginmanager.installer.impls.autoremove;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstallerArgument;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 自動削除の引数を格納するクラスです。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AutoRemoveArgument extends AbstractInstallerArgument
{
    /**
     * 自動削除から除外するプラグインのリストです。
     */
    @NotNull
    private final List<String> excludePlugins;

    private AutoRemoveArgument(@NotNull List<String> excludePlugins)
    {
        this.excludePlugins = excludePlugins;
    }

    public AutoRemoveArgument()
    {
        this(new ArrayList<>());
    }

    /**
     * 除外するプラグインを追加します。
     *
     * @param pluginName 除外するプラグインの名前
     * @return このインスタンス
     */
    public AutoRemoveArgument addExcludePlugin(@NotNull String pluginName)
    {
        excludePlugins.add(pluginName);
        return this;
    }

    /**
     * 除外するプラグインを追加します。
     *
     * @param plugin 除外するプラグイン
     * @return このインスタンス
     */
    public AutoRemoveArgument addExcludePlugin(@NotNull Plugin plugin)
    {
        this.addExcludePlugin(plugin.getName());
        return this;
    }


}
