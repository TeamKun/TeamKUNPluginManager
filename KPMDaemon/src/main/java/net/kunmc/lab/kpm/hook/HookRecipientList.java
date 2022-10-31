package net.kunmc.lab.kpm.hook;

import lombok.Getter;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.kpminfo.InvalidInformationFileException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * KPMフックの受け取りを行うクラスを管理するクラスです。
 */
public class HookRecipientList extends ArrayList<KPMHookRecipient>
{
    @Getter
    private final KPMDaemon daemon;
    @Getter
    private final HookExecutor executor;

    @Getter
    private final List<String> reservedHookClasses;

    public HookRecipientList(@NotNull KPMDaemon daemon, @NotNull HookExecutor executor)
    {
        this.daemon = daemon;
        this.executor = executor;
        this.reservedHookClasses = new ArrayList<>();
    }

    /**
     * フックを実行します。
     *
     * @param hook フック
     */
    public void runHook(KPMHook hook)
    {
        if (!this.reservedHookClasses.isEmpty())
        {
            try
            {
                this.bakeHooks(this.daemon);
            }
            catch (InvalidInformationFileException e)
            {
                throw new IllegalStateException("Failed to bake hook listeners: " + e.getMessage(), e);
            }
        }

        for (KPMHookRecipient recipient : this)
            this.executor.runHook(recipient, hook);
    }

    public void add(@NotNull String className)
    {
        this.reservedHookClasses.add(className);
    }

    /**
     * 予約クラス名からフックを作成します。
     *
     * @param daemon KPMデーモンのインスタンス
     * @throws InvalidInformationFileException 予約クラス名が無効な場合
     */
    public void bakeHooks(@NotNull KPMDaemon daemon) throws InvalidInformationFileException
    {
        Iterator<String> iterator = this.reservedHookClasses.iterator();
        while (iterator.hasNext())
        {
            String className = iterator.next();

            try
            {
                this.daemon.getLogger().setLevel(Level.OFF);
                Class<?> hookClass = Class.forName(className);
                this.daemon.getLogger().setLevel(Level.INFO);

                if (!KPMHookRecipient.class.isAssignableFrom(hookClass))
                    throw new InvalidInformationFileException("Class " + className + " is not a KPMHookRecipient.");

                Constructor<? extends KPMHookRecipient> constructor =
                        hookClass.asSubclass(KPMHookRecipient.class).getConstructor(KPMDaemon.class);

                this.add(constructor.newInstance(daemon));
            }
            catch (ClassNotFoundException e)
            {
                throw new InvalidInformationFileException("Hook recipient class was not found: " + className, e);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                throw new InvalidInformationFileException("Failed to create an instance of hook recipient class: " +
                        className, e);
            }
            catch (NoSuchMethodException e)
            {
                throw new InvalidInformationFileException("Hook recipient class must have a constructor with" +
                        " a single parameter of type KPMDaemon: " + className, e);
            }
            finally
            {
                this.daemon.getLogger().setLevel(Level.INFO);

                iterator.remove();
            }
        }
    }
}
