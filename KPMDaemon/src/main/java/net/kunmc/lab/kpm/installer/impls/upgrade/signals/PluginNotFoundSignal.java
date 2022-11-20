package net.kunmc.lab.kpm.installer.impls.upgrade.signals;

import lombok.Data;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * 指定されたプラグインが見つからなかったことを示すシグナルです。
 */
@Data
public class PluginNotFoundSignal implements Signal
{
    /**
     * 指定されたプラグインの名前です。
     */
    @NotNull
    private final String specifiedPluginName;

    /**
     * アップグレードを続けるかどうかのフラグです。
     * このフラグをfalseにすると、アップグレードは中断され、{@link net.kunmc.lab.kpm.installer.impls.upgrade.PluginUpgrader}は失敗します。
     */
    private boolean continueUpgrade;

    public PluginNotFoundSignal(@NotNull String specifiedPluginName)
    {
        this.specifiedPluginName = specifiedPluginName;
        this.continueUpgrade = true;
    }
}
