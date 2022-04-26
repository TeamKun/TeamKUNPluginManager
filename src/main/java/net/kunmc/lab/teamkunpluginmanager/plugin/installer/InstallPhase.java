package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

public enum InstallPhase
{
    // General phases
    STARTED,
    INITIALIZING,

    // Install phases
    QUERY_RESOLVING,
    MULTIPLE_RESULT_RESOLVING,
    START_DOWNLOADING,
    DOWNLOADING

}
