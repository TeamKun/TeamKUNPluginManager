package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.DependencyElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
public class DependsLoadOrderComputedSignal implements InstallerSignal
{
    @NotNull
    private final ArrayList<DependencyElement> dependencies;
}
