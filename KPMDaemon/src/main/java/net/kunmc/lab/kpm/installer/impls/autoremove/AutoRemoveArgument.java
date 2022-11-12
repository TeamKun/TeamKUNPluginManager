package net.kunmc.lab.kpm.installer.impls.autoremove;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import net.kunmc.lab.kpm.installer.AbstractInstallerArgument;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 自動削除の引数を格納するクラスです。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class AutoRemoveArgument extends AbstractInstallerArgument
{
    /**
     * 自動削除から除外するプラグインのリストです。
     */
    @NotNull
    @Singular("exclude")
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
     * @param plugin 除外するプラグイン
     * @return このインスタンス
     */
    public AutoRemoveArgument addExcludePlugin(@NotNull Plugin plugin)
    {
        this.excludePlugins.add(plugin.getName());
        return this;
    }


}
