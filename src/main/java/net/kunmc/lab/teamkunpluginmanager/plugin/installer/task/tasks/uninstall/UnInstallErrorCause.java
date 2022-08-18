package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall;

public enum UnInstallErrorCause
{
    SOME_UNINSTALL_FAILED,

    INTERNAL_UNINSTALL_OK,

    INTERNAL_PLUGIN_DISABLE_FAILED,
    INTERNAL_CLASS_UNLOAD_FAILED,
}
