package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install;

public enum PluginsInstallState
{
    INITIALIZED,

    PLUGIN_RELOCATING,
    PLUGIN_LOADING,
    ONLOAD_RUNNING,
    PLUGIN_ENABLING,
}
