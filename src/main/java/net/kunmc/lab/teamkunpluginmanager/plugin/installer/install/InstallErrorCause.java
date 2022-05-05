package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

public enum InstallErrorCause
{
    PHASE_EXCEPTION_OCCURRED,
    PHASE_FAILED,

    // Environment errors
    IGNORED_PLUGIN,
    ALREADY_INSTALLED,

    EXCEPTION_OCCURRED,
}
