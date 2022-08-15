package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install;

public enum InstallErrorCause
{
    // Tasks
    TASK_EXCEPTION_OCCURRED,
    TASK_FAILED,

    // Environment errors
    PLUGIN_IGNORED,
    PLUGIN_ALREADY_INSTALLED,

    // Exceptions
    EXCEPTION_OCCURRED,
}
