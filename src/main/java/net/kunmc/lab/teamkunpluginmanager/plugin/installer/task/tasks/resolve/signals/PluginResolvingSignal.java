package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;

/**
 * プラグインの解決中であることを示すシグナルです。
 */
@Data
@AllArgsConstructor
public class PluginResolvingSignal implements InstallerSignal
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
