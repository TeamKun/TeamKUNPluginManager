package org.kunlab.kpm.installer.impls.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.InstallResultImpl;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;

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

    public GarbageCleanSucceedResult(InstallProgress<CleanTasks, ? extends Installer<? extends InstallerArgument, ? extends Enum<?>, CleanTasks>> progress,
                                     @NotNull List<Path> deletedFiles, @NotNull List<Path> deleteFailedFiles)
    {
        super(true, progress);
        this.deletedFiles = deletedFiles;
        this.deleteFailedFiles = deleteFailedFiles;
    }
}
