package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

public enum InstallTasks
{
    STARTED,
    INITIALIZING,

    RESOLVING_QUERY,
    DOWNLOADING,
    LOADING_PLUGIN_DESCRIPTION,
    CHECKING_ENVIRONMENT,
    REMOVING_OLD_PLUGIN,
    COLLECTING_DEPENDENCIES,
    COMPUTING_LOAD_ORDER,
    INSTALLING_PLUGINS,
}
