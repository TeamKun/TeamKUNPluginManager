package org.kunlab.kpm.installer.impls.upgrade.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.impls.upgrade.PluginUpgrader;
import org.kunlab.kpm.signal.Signal;

/**
 * 指定されたプラグインが見つからなかったことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginNotFoundSignal extends Signal
{
    /**
     * 指定されたプラグインの名前です。
     */
    @NotNull
    private final String specifiedPluginName;

    /**
     * アップグレードを続けるかどうかのフラグです。
     * このフラグをfalseにすると、アップグレードは中断され、{@link PluginUpgrader}は失敗します。
     */
    private boolean continueUpgrade;

    public PluginNotFoundSignal(@NotNull String specifiedPluginName)
    {
        this.specifiedPluginName = specifiedPluginName;
        this.continueUpgrade = true;
    }
}
