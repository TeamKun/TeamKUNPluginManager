package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall;

public enum UnInstallState
{
    INITIALIZED,
    UNINSTALLING,

    RECIPES_UNREGISTERING,
    COMMANDS_UNPATCHING,
    PLUGIN_DISABLING,

    REMOVING_FROM_BUKKIT,
    CLASSES_UNLOADING
}
