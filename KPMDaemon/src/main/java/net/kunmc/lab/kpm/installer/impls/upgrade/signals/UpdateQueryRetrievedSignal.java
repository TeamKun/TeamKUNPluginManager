package net.kunmc.lab.kpm.installer.impls.upgrade.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * アップグレードのクエリを取得したことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateQueryRetrievedSignal extends Signal
{
    /**
     * アップグレード対象のプラグインです。
     */
    @NotNull
    private final Plugin plugin;

    /**
     * アップグレードに使用するクエリです。
     */
    @Nullable
    private String query;
}
