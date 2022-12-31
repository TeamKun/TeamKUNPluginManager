package net.kunmc.lab.kpm.installer.impls.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.InstallResultImpl;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要なデータの削除に成功したことを表す結果です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class GarbageCleanSucceedResult extends InstallResultImpl<CleanTasks>
{
    /**
     * 削除された不要なデータのパスのリストです。
     */
    @NotNull
    List<Path> deletedFiles;
    /**
     * 削除に失敗した不要なデータのパスのリストです。
     */
    @NotNull
    List<Path> deleteFailedFiles;

    public GarbageCleanSucceedResult(InstallProgress<CleanTasks, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, CleanTasks>> progress,
                                     @NotNull List<Path> deletedFiles, @NotNull List<Path> deleteFailedFiles)
    {
        super(true, progress);
        this.deletedFiles = deletedFiles;
        this.deleteFailedFiles = deleteFailedFiles;
    }
}
