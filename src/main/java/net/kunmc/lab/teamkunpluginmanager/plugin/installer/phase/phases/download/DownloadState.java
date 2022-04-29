package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseEnum;

public enum DownloadState implements PhaseEnum
{
    INITIALIZED,

    START_DOWNLOADING,
    DOWNLOADING,
}
