package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseEnum;

public enum PluginResolveState implements PhaseEnum
{
    INITIALIZED,

    PRE_RESOLVING,
    PRE_RESOLVE_FINISHED,

    MULTI_RESOLVING,

    RESOLVE_FINISHED
}
