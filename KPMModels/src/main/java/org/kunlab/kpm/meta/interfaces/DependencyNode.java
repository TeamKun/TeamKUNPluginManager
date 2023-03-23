package org.kunlab.kpm.meta.interfaces;

import org.kunlab.kpm.meta.DependType;

/**
 * 依存関係のノードです。
 */
public interface DependencyNode
{
    /**
     * 依存しているプラグインの名前です。
     *
     * @return 依存しているプラグインの名前
     */
    String getPlugin();

    /**
     * 依存先のプラグインの名前です。
     *
     * @return 依存先のプラグインの名前
     */
    String getDependsOn();

    /**
     * 依存の種類です。
     *
     * @return 依存の種類
     */
    DependType getDependType();
}
