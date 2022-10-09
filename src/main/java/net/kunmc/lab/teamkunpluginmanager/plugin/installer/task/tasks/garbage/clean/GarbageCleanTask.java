package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean.signal.GarbageDeleteSkippedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean.signal.GarbageDeletingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean.signal.GarbageEnumeratedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean.signal.InvalidIntegritySignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandleManager;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * 不要データ削除を行うタスクです。
 */
public class GarbageCleanTask extends InstallTask<GarbageCleanArgument, GarbageCleanResult>
{
    private GarbageCleanState status;

    public GarbageCleanTask(@NotNull InstallProgress<?, ?> progress, @NotNull SignalHandleManager signalHandler)
    {
        super(progress, signalHandler);

        this.status = GarbageCleanState.INITIALIZED;
    }

    @Override
    public @NotNull GarbageCleanResult runTask(@NotNull GarbageCleanArgument arguments)
    {
        List<Path> paths = arguments.getPaths();
        HashMap<Path, Boolean> result = new HashMap<>();

        if (arguments.getPaths().isEmpty())
            return new GarbageCleanResult(false, this.status,
                    GarbageCleanErrorCause.NO_GARBAGE, result
            );

        GarbageEnumeratedSignal signal = new GarbageEnumeratedSignal(paths);
        this.postSignal(signal);
        if (signal.isCancel())
            return new GarbageCleanResult(false, this.status,
                    GarbageCleanErrorCause.CANCELLED, result
            );

        paths = signal.getGarbageDatas(); // results may be modified by signal

        this.status = GarbageCleanState.DELETING_GARBAGE;
        for (Path path : paths)
        {
            int state = this.removeOne(path);
            if (state == -1)
            {
                this.postSignal(new InvalidIntegritySignal());
                return new GarbageCleanResult(false, this.status,
                        GarbageCleanErrorCause.INVALID_INTEGRITY, result
                );
            }

            result.put(path, state == 0);
        }

        if (result.values().stream().parallel().noneMatch(b -> b))
            return new GarbageCleanResult(false, this.status,
                    GarbageCleanErrorCause.ALL_DELETE_FAILED, result
            );

        return new GarbageCleanResult(true, this.status, null, result);
    }

    /**
     * 一つの不要データを削除します。
     *
     * @param path 削除する不要データのパスです。
     * @return 0: 削除成功   1: 削除失敗   -1: 致命的なエラー
     */
    private int removeOne(Path path)
    {
        GarbageDeletingSignal.Pre signal = new GarbageDeletingSignal.Pre(path);
        this.postSignal(signal);
        if (signal.isSkip())
        {
            this.postSignal(new GarbageDeleteSkippedSignal(path));
            return 1;
        }

        try
        {
            FileUtils.forceDelete(path.toFile());

            this.postSignal(new GarbageDeletingSignal.Post(path));
            return 0;
        }
        catch (FileNotFoundException e)
        {
            // Illegal state, so protect the system.
            return -1;
        }
        catch (Exception ignored)
        {
        }

        return 1;
    }
}
