package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

public enum InstallErrorCause
{
    PHASE_EXCEPTION_OCCURRED,
    PHASE_FAILED,

    // Environment errors
    PLUGIN_IGNORED,
    PLUGIN_ALREADY_INSTALLED,

    EXCEPTION_OCCURRED,
}
