package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

public enum InstallErrorCause
{
    // Phases
    PHASE_EXCEPTION_OCCURRED,
    PHASE_FAILED,

    // Environment errors
    PLUGIN_IGNORED,
    PLUGIN_ALREADY_INSTALLED,

    // Exceptions
    EXCEPTION_OCCURRED,
}
