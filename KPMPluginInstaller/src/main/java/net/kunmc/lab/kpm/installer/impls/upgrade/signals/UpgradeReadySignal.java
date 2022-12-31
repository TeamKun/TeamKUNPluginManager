package net.kunmc.lab.kpm.installer.impls.upgrade.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.resolver.result.AbstractSuccessResult;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * アップグレードの準備が完了したことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UpgradeReadySignal extends Signal
{
    private final List<ResolvedPluginElement> plugins;

    public UpgradeReadySignal(Map<Plugin, SuccessResult> resolveResult)
    {
        this.plugins = resolveResult.entrySet().stream()
                .map(e -> new ResolvedPluginElement(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public void setContinueUpgrade(boolean value)
    {
        this.plugins.forEach(e -> e.setContinueUpgrade(value));
    }

    @Data
    @AllArgsConstructor
    public static class ResolvedPluginElement
    {
        /**
         * アップグレード対象のプラグインです。
         */
        private final Plugin plugin;
        /**
         * アップグレードのために解決された解決結果です。
         */
        private final SuccessResult resolveResult;

        private boolean continueUpgrade;

        public ResolvedPluginElement(Plugin plugin, AbstractSuccessResult resolveResult)
        {
            this(plugin, resolveResult, true);
        }
    }
}
