package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseEnum;

public enum DownloadErrorCause implements PhaseEnum
{
    ILLEGAL_HTTP_RESPONSE,
    NO_BODY_IN_RESPONSE,
    IO_EXCEPTION,
    UNKNOWN_ERROR
}
