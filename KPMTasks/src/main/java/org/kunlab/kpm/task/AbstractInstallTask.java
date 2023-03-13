package org.kunlab.kpm.task;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.interfaces.InstallTask;
import org.kunlab.kpm.task.interfaces.TaskArgument;
import org.kunlab.kpm.task.interfaces.TaskResult;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@AllArgsConstructor
public abstract class AbstractInstallTask<A extends TaskArgument, R extends TaskResult<? extends Enum<?>, ? extends Enum<?>>> implements InstallTask<A, R>
{

    @NotNull
    protected final InstallProgress<? extends Enum<?>, ? extends Installer<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>> progress;

    @NotNull
    private final SignalHandleManager signalHandler;

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
                throw new IllegalStateException(ex);
            }
        });

        try
        {
            barrier.await();
        }
        catch (BrokenBarrierException | InterruptedException e)
        {
            throw new IllegalStateException(e);
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
            throw new IllegalStateException(e);
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
            throw new IllegalStateException(e);
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
