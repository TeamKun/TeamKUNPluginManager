package org.kunlab.kpm.installer.impls.upgrade.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.signal.Signal;

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
