package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

public enum InstallPhases
{
    STARTED,
    INITIALIZING,

    RESOLVING_QUERY,
    DOWNLOADING,
    LOADING_PLUGIN_DESCRIPTION,
    CHECKING_ENVIRONMENT,
    REMOVING_OLD_PLUGIN,
    COLLECTING_DEPENDENCIES,
}