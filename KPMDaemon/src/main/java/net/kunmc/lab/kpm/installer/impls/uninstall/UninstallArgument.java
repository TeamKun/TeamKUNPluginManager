package net.kunmc.lab.kpm.installer.impls.uninstall;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.installer.AbstractInstallerArgument;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * アンインストールの引数を格納するクラスです。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class UninstallArgument extends AbstractInstallerArgument
{
    /**
     * アンインストールするプラグインの名前です。
     * {@link #plugins} またはこのフィールドのどちらかが指定されている必要があります。
     */
    @Nullable
    @Unmodifiable
    private final List<String> pluginNames;

    /**
     * アンインストールするプラグインのインスタンスです。
     * {@link #pluginNames} またはこのフィールドのどちらかが指定されている必要があります。
     */
    @Nullable
    @Unmodifiable
    private final List<Plugin> plugins;

    public static UninstallArgumentBuilder builder(Plugin plugin)
    {
        return new UninstallArgumentBuilder()
                .plugins(Collections.singletonList(plugin));
    }

    public static UninstallArgumentBuilder builder(String pluginName)
    {
        return new UninstallArgumentBuilder()
                .pluginNames(Collections.singletonList(pluginName));
    }

    public static UninstallArgumentBuilder builder(Plugin... plugins)
    {
        return new UninstallArgumentBuilder()
                .plugins(Arrays.asList(plugins));
    }

    public static UninstallArgumentBuilder builder(String... pluginNames)
    {
        return new UninstallArgumentBuilder()
                .pluginNames(Arrays.asList(pluginNames));
    }
}
