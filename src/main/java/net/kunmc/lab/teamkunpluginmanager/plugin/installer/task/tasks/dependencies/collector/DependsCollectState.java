package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector;

public enum DependsCollectState
{
    INITIALIZED,

    RESOLVING_DEPENDS,
    DOWNLOADING_DEPENDS,

    COLLECTING_DEPENDS_DEPENDS // Dependency's depends.
}
