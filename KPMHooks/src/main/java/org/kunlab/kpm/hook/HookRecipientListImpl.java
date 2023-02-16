package org.kunlab.kpm.hook;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.hook.HookExecutor;
import org.kunlab.kpm.interfaces.hook.HookRecipientList;
import org.kunlab.kpm.interfaces.hook.KPMHook;
import org.kunlab.kpm.interfaces.hook.KPMHookRecipient;
import org.kunlab.kpm.kpminfo.InvalidInformationFileException;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class HookRecipientListImpl extends ArrayList<KPMHookRecipient> implements HookRecipientList
{
    @Getter
    private final KPMRegistry registry;
    private final HookExecutor executor;

    private final List<String> reservedHookClasses;

    public HookRecipientListImpl(@NotNull KPMRegistry registry, @NotNull HookExecutor executor)
    {
        this.registry = registry;
        this.executor = executor;
        this.reservedHookClasses = new ArrayList<>();
    }

    @Override
    public void runHook(KPMHook hook)
    {
        if (!this.reservedHookClasses.isEmpty())
        {
            try
            {
                this.bakeHooks(this.registry);
            }
            catch (InvalidInformationFileException e)
            {
                throw new IllegalStateException("Failed to bake hook listeners: " + e.getMessage(), e);
            }
        }

        for (KPMHookRecipient recipient : this)
            this.executor.runHook(recipient, hook);
    }

    @Override
    public void add(@NotNull String className)
    {
        this.reservedHookClasses.add(className);
    }

    @Override
    public void bakeHooks(@Nonnull KPMRegistry registry) throws InvalidInformationFileException
    {
        Iterator<String> iterator = this.reservedHookClasses.iterator();
        while (iterator.hasNext())
        {
            String className = iterator.next();

            try
            {
                this.registry.getLogger().setLevel(Level.OFF);
                Class<?> hookClass = Class.forName(className);
                this.registry.getLogger().setLevel(Level.INFO);

                if (!KPMHookRecipientBase.class.isAssignableFrom(hookClass))
                    throw new InvalidInformationFileException("Class " + className + " is not a KPMHookRecipient.");

                Constructor<? extends KPMHookRecipient> constructor =
                        hookClass.asSubclass(KPMHookRecipientBase.class).getConstructor(KPMRegistry.class);

                this.add(constructor.newInstance(this.registry));
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
            catch (NoClassDefFoundError e)
            {
                String message;
                if (e.getMessage().contains("net/kunmc/lab/kpm/hook/KPMHookRecipient"))
                    message = "incompatible kpm version?";
                else
                    message = "unknown";

                throw new InvalidInformationFileException("Failed to load hooks for " + className + ": " + message, e);
            }
            finally
            {
                this.registry.getLogger().setLevel(Level.INFO);

                iterator.remove();
            }
        }
    }
}
