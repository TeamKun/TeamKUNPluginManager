package org.kunlab.kpm.commands.debug.deptree;

import lombok.Data;
import org.kunlab.kpm.meta.DependType;
import org.kunlab.kpm.meta.interfaces.DependencyNode;

@Data
class DependencyNodeMock implements DependencyNode
{
    private final String plugin;

    private final String dependsOn;

    private final DependType dependType;
}
