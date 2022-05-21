package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.signals.PluginLoadedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.signals.PluginOnEnableRunningSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.signals.PluginOnLoadRunningSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.signals.PluginRelocatingSignal;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class PluginsInstallPhase extends InstallPhase<PluginsInstallArgument, PluginsInstallResult>
{
    private static final Path PLUGIN_DIR;

    static
    {
        PLUGIN_DIR = TeamKunPluginManager.getPlugin().getDataFolder().toPath().getParent();
    }

    private PluginsInstallState state;

    public PluginsInstallPhase(@NotNull InstallProgress<?> progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.state = PluginsInstallState.INITIALIZED;
    }

    @Override
    public @NotNull PluginsInstallResult runPhase(@NotNull PluginsInstallArgument arguments)
    {
        List<DependencyElement> dependencies = arguments.getDependencies();

        // Install dependencies
        for (DependencyElement dependency : dependencies)
        {
            PluginDescriptionFile pluginDescription = dependency.getPluginDescription();
            Path path = dependency.getPluginPath();

            PluginsInstallResult result = this.installOne(path, pluginDescription);
            if (result != null)
                return result;
        }

        // Install plugin after dependencies installed
        PluginsInstallResult result = this.installOne(arguments.getPluginPath(), arguments.getPluginDescription());
        if (result != null)
            return result;
        else
            return new PluginsInstallResult(true, this.state, null);
    }

    @Nullable
    private PluginsInstallResult installOne(@NotNull Path path, @NotNull PluginDescriptionFile pluginDescription)
    {
        this.state = PluginsInstallState.RELOCATING_PLUGIN;


        // Relocate plugin
        String fileName = pluginDescription.getName() + "-" + pluginDescription.getVersion() + ".jar";
        Path targetPath = PLUGIN_DIR.resolve(fileName);

        PluginsInstallResult mayError = this.movePlugin(path, targetPath);

        if (mayError != null)
            return mayError;

        // Load plugin
        this.state = PluginsInstallState.LOADING_PLUGIN;

        Plugin target;
        try
        {
            target = Bukkit.getPluginManager().loadPlugin(path.toFile());
            assert target != null;
            this.postSignal(new PluginLoadedSignal(target));

            this.progress.addInstalled(target.getName());

            this.postSignal(new PluginOnLoadRunningSignal(target));
            this.state = PluginsInstallState.RUNNING_ONLOAD;
            target.onLoad();

        }
        catch (InvalidDescriptionException e)
        {
            e.printStackTrace();
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.INVALID_PLUGIN_DESCRIPTION);
        }
        catch (InvalidPluginException e)
        {
            e.printStackTrace();
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.INVALID_PLUGIN);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.EXCEPTION_OCCURRED);
        }

        // Enable plugin
        this.state = PluginsInstallState.RUNNING_ONENABLE;
        this.postSignal(new PluginOnEnableRunningSignal(target));
        Bukkit.getPluginManager().enablePlugin(target);

        return null;  // Success
    }

    @Nullable
    private String generateSHA1(@NotNull Path path) throws IOException
    {
        try (FileInputStream inputStream = new FileInputStream(path.toFile()))
        {
            return DigestUtils.sha1Hex(inputStream);
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
    }

    private boolean moveFile(@NotNull Path source, @NotNull Path target, boolean overwrite) throws IOException
    {
        try
        {
            if (overwrite)
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            else
                Files.move(source, target);

            return true;
        }
        catch (FileAlreadyExistsException e)
        {
            String sourceHash = generateSHA1(source);
            String targetHash = generateSHA1(target);

            if (sourceHash == null || targetHash == null)
                return false;

            if (sourceHash.equals(targetHash))
                return true;
            else
                return moveFile(source, target, true);
        }
    }

    @Nullable
    private PluginsInstallResult movePlugin(@NotNull Path source, @NotNull Path target)
    {
        PluginRelocatingSignal signal = new PluginRelocatingSignal(source, target);
        this.postSignal(signal);

        target = signal.getTarget(); // May be changed by signal

        try
        {
            if (!moveFile(source, target, false))
                return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.RELOCATE_FAILED);
            else
                return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();

            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.IO_EXCEPTION_OCCURRED);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.RELOCATE_FAILED);
        }
    }
}
