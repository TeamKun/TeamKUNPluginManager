package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.FailedReason;

public enum DownloadErrorCause
{
    ILLEGAL_HTTP_RESPONSE,
    NO_BODY_IN_RESPONSE,
    IO_EXCEPTION,
    UNKNOWN_ERROR;

    public FailedReason toFailedReason()
    {
        switch (this)
        {
            case ILLEGAL_HTTP_RESPONSE:
                return FailedReason.ILLEGAL_RESPONSE_CODE;
            case NO_BODY_IN_RESPONSE:
                return FailedReason.NO_RESPONSE_BODY;
            case IO_EXCEPTION:
                return FailedReason.IO_EXCEPTION_OCCURRED;
            case UNKNOWN_ERROR:
            default:
                return FailedReason.ILLEGAL_INTERNAL_STATE;
        }
    }
}
