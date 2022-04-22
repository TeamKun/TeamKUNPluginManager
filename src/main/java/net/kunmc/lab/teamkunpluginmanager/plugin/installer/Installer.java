package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignalHandler;

public class Installer
{
    private final InstallerSignalHandler signalHandler;
    private final PlumbingInstaller installer;

    public Installer(InstallerSignalHandler signalHandler)
    {
        this.signalHandler = signalHandler;
        this.installer = new PlumbingInstaller(this, signalHandler);
    }
}
