package org.kunlab.kpm.meta;

import lombok.Value;
import org.kunlab.kpm.meta.interfaces.DependencyNode;

@Value
class DependencyNodeImpl implements DependencyNode
{
    String plugin;

    String dependsOn;

    DependType dependType;
}
