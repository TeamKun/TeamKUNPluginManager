package net.kunmc.lab.kpm.task.tasks.resolve.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.interfaces.resolver.PluginResolver;
import net.kunmc.lab.kpm.signal.Signal;

/**
 * プラグインの解決中であることを示すシグナルです。
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PluginResolvingSignal extends Signal
{
    /**
     * 解決に使用されるプラグインリゾルバです。
     */
    private final PluginResolver resolver;
    /**
     * 解決する際に使用されるクエリです。
     */
    private String query;
}
