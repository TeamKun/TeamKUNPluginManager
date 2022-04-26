package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

public enum FailedReason
{
    // Resolving errors
    GOT_ERROR_RESULT,

    // Net errors
    ILLEGAL_RESPONSE_CODE,
    NO_RESPONSE_BODY,

    // General internal errors
    ILLEGAL_INTERNAL_STATE,
    IO_EXCEPTION_OCCURRED,
}
