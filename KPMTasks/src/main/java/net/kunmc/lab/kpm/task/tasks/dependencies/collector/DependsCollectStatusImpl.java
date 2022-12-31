package net.kunmc.lab.kpm.task.tasks.dependencies.collector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.kpm.interfaces.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.kpm.interfaces.task.tasks.dependencies.collector.DependsCollectStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DependsCollectStatusImpl implements DependsCollectStatus
{
    @NotNull
    private final String installId;
    @NotNull
    @Getter(AccessLevel.PRIVATE)
    private final HashMap<String, DependencyElement> enumeratedDependencies;

    @NotNull
    private String pluginName;

    public DependsCollectStatusImpl(InstallProgress<? extends Enum<?>, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>> progress)
    {
        this.installId = progress.getInstallActionID();
        this.enumeratedDependencies = new HashMap<>();
        this.pluginName = "undefined-" + this.installId;
    }

    @Override
    public void addDependency(@NotNull String dependencyName)
    {
        if (!this.enumeratedDependencies.containsKey(dependencyName))
            this.enumeratedDependencies.put(dependencyName, null);
    }

    @Override
    public boolean isCollected(@NotNull String dependencyName)
    {
        return this.enumeratedDependencies.containsKey(dependencyName);
    }

    @Override
    public boolean isErrors()
    {
        return this.enumeratedDependencies.containsValue(null);
    }

    @Override
    public void onCollect(@NotNull String dependencyName, DependencyElement dependencyElement)
    {
        if (this.enumeratedDependencies.containsKey(dependencyName))
            this.enumeratedDependencies.put(dependencyName, dependencyElement);
    }

    @Override
    public List<DependencyElement> getCollectedDependencies()
    {
        return this.enumeratedDependencies.entrySet().stream().parallel()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCollectFailedDependencies()
    {
        return this.enumeratedDependencies.entrySet().stream().parallel()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
