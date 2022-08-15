package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install;

public enum PluginsInstallState
{
    INITIALIZED,

    RELOCATING_PLUGIN,
    LOADING_PLUGIN,
    RUNNING_ONLOAD,
    RUNNING_ONENABLE,
}
