package net.kunmc.lab.kpm.installer.task;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.kpm.signal.Signal;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * インストールに使用するタスクの基底クラスです。
 *
 * @param <A> タスクの引数の型
 * @param <R> タスクの結果の型
 */
@AllArgsConstructor
public abstract class InstallTask<A extends TaskArgument, R extends TaskResult<? extends Enum<?>, ? extends Enum<?>>>
{
    /**
     * インストールごとに生成される、インストール進捗状況です。
     */
    @NotNull
    protected final InstallProgress<? extends Enum<?>, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>> progress;
    /**
     * タスクからスローされるシグナルを受け取るハンドラーです。
     */
    @NotNull
    private final SignalHandleManager signalHandler;

    /**
     * タスクを実行します。
     *
     * @param arguments タスクの引数です。
     * @return タスクの結果です。
     */
    public abstract @NotNull R runTask(@NotNull A arguments);

    protected void postSignal(@NotNull Signal signal)
    {
        this.signalHandler.handleSignal(signal);
    }

    protected void runSyncThrowing(@NotNull ThrowingRunnable runnable) throws Exception
    {
        CyclicBarrier barrier = new CyclicBarrier(2);

        AtomicReference<Exception> errorRef = new AtomicReference<>();
        Runner.run(() -> {
            runnable.run();
            barrier.await();
        }, (e, task) -> {
            errorRef.set(e);
            try
            {
                barrier.await();
            }
            catch (InterruptedException | BrokenBarrierException ex)
            {
                throw new RuntimeException(ex);
            }
        });

        try
        {
            barrier.await();
        }
        catch (BrokenBarrierException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        if (errorRef.get() != null)
            throw errorRef.get();
    }

    protected <T> T runSyncThrowing(@NotNull ThrowingSupplier<T> supplier) throws Exception
    {
        AtomicReference<T> resultRef = new AtomicReference<>();
        this.runSyncThrowing(() -> resultRef.set(supplier.get()));

        return resultRef.get();
    }

    protected void runSync(@NotNull Runnable runnable)
    {
        try
        {
            this.runSyncThrowing(runnable::run);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected <T> T runSync(@NotNull Supplier<T> supplier)
    {
        try
        {
            return this.runSyncThrowing(supplier::get);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    protected interface ThrowingSupplier<T>
    {
        T get() throws Exception;
    }

    @FunctionalInterface
    protected interface ThrowingRunnable
    {
        void run() throws Exception;
    }
}
