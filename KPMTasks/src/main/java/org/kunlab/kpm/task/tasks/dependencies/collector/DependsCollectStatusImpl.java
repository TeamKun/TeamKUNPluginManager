package org.kunlab.kpm.task.tasks.dependencies.collector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;
import org.kunlab.kpm.task.interfaces.dependencies.collector.DependsCollectStatus;

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
    private final Map<String, DependencyElement> enumeratedDependencies;

    @NotNull
    private String pluginName;

    public DependsCollectStatusImpl(InstallProgress<? extends Enum<?>, ? extends Installer<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>> progress)
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
