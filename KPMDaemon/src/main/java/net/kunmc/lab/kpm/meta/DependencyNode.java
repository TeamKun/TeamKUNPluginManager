package net.kunmc.lab.kpm.meta;

import lombok.Value;

/**
 * 依存関係のノードです。
 */
@Value
public class DependencyNode
{
    /**
     * 依存しているプラグインの名前です。
     */
    String plugin;

    /**
     * 依存先のプラグインの名前です。
     */
    String dependsOn;

    /**
     * 依存の種類です。
     */
    DependType dependType;
}
