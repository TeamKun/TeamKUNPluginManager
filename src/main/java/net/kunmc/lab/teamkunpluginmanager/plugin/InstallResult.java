package net.kunmc.lab.teamkunpluginmanager.plugin;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class InstallResult
{

    String fileName;
    String pluginName;
    int add;
    int remove;
    int modify;
    boolean success;

    public InstallResult(boolean success)
    {
        this("", "", 0, 0, 0, success);
    }

    public InstallResult(int add, int remove, int modify, boolean success)
    {
        this("", "", add, remove, modify, success);
    }

    public InstallResult(String fileName, String pluginName)
    {
        this(fileName, pluginName, 0, 0, 0, true);
    }

}
