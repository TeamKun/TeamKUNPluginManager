package org.kunlab.kpm.commands.debug;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Question;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.installer.interfaces.PluginInstaller;
import org.kunlab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.download.signals.DownloadProgressSignal;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class DebugSignalHandler
{
    private static final int MAX_INDENT = 5;

    private final Terminal terminal;

    private static <T extends Signal> void handleInputSignals(T signal, Terminal terminal)
    {
        try
        {
            if (signal instanceof AlreadyInstalledPluginSignal)
            {
                AlreadyInstalledPluginSignal alreadyInstalledPluginSignal = (AlreadyInstalledPluginSignal) signal;

                Question question = terminal.getInput().showYNQuestion("Newly install?");
                alreadyInstalledPluginSignal.setReplacePlugin(question.waitAndGetResult().test(QuestionAttribute.YES));
            }
            else if (signal instanceof IgnoredPluginSignal)
            {
                IgnoredPluginSignal ignoredPluginSignal = (IgnoredPluginSignal) signal;

                Question question = terminal.getInput().showYNQuestion("Cancel?");
                ignoredPluginSignal.setContinueInstall(question.waitAndGetResult().test(QuestionAttribute.NO));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static <T extends Signal> void printSignal(T signal, Terminal terminal)
    {
        terminal.writeLine("====================");
        terminal.info("On Signal: " + signal.getClass().getSimpleName());
        terminal.info("Values:");
        varDump(signal, terminal);
        terminal.writeLine("====================");
    }

    private static void printField(String fieldName, Object o, Terminal terminal, int indent, boolean noValue, boolean noName)
    {
        String typePrefix = getTypePrefix(o);

        String indentStr = StringUtils.repeat("  ", indent);

        String prefix = indentStr + ChatColor.YELLOW + (noName ? "     ": fieldName + " ==> ") + typePrefix;

        if (noValue)
            terminal.writeLine(prefix);
        else if (o instanceof Map.Entry)
        {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            terminal.writeLine(prefix + entry.getKey() + ": " + entry.getValue());
        }
        else
            terminal.writeLine(prefix + o);
    }

    private static void printString(String fieldName, String value, Terminal terminal, int indent)
    {
        String indentStr = StringUtils.repeat("  ", indent);
        terminal.writeLine(indentStr + ChatColor.YELLOW + fieldName + " ==> " + value);
    }

    private static boolean isCompatible(Object o)
    {
        if (o == null)
            return true;

        Class<?> clazz = o.getClass();

        return clazz.isPrimitive() ||
                String.class.isAssignableFrom(clazz) ||
                Integer.class.isAssignableFrom(clazz) ||
                Boolean.class.isAssignableFrom(clazz) ||
                Double.class.isAssignableFrom(clazz) ||
                Float.class.isAssignableFrom(clazz) ||
                Long.class.isAssignableFrom(clazz) ||
                Short.class.isAssignableFrom(clazz) ||
                Byte.class.isAssignableFrom(clazz) ||
                Character.class.isAssignableFrom(clazz) ||
                clazz.isArray() ||
                clazz.isEnum() ||
                Map.class.isAssignableFrom(clazz) ||
                Collection.class.isAssignableFrom(clazz);
    }

    private static String getTypePrefix(Object o)
    {
        if (o == null)
            return ChatColor.GRAY + "null";

        String prefix = "";

        Class<?> clazz;
        if (o instanceof Class<?>)
            clazz = (Class<?>) o;
        else
            clazz = o.getClass();

        if (clazz.isArray())
            prefix = "[" + getTypePrefix(clazz.getComponentType());

        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz))
            return prefix + ChatColor.BLUE + "I";
        else if (String.class.isAssignableFrom(clazz))
            return prefix + ChatColor.GOLD + "S";
        else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
            return prefix + ChatColor.GREEN + "Z";
        else if (Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz))
            return prefix + ChatColor.DARK_PURPLE + "C";
        else if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz))
            return prefix + ChatColor.AQUA + "D";
        else if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz))
            return prefix + ChatColor.DARK_AQUA + "F";
        else if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz))
            return prefix + ChatColor.DARK_GREEN + "J";
        else if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz))
            return prefix + ChatColor.DARK_RED + "S";
        else if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz))
            return prefix + ChatColor.DARK_PURPLE + "B";
        else if (clazz.isEnum())
            return prefix + ChatColor.DARK_PURPLE + "E";
        else if (clazz.isPrimitive())
            return prefix + ChatColor.GRAY + clazz.getSimpleName();
        else
            return prefix + "L" + clazz.getName() + ";: ";
    }

    private static void varDumpRecursive(Field field, Object value, Terminal terminal, int indent, boolean noName)
            throws IllegalAccessException
    {
        if (!isCompatible(value))
        {

            printField(field.getName(), value, terminal, indent, false, noName);
            varDump(value, terminal, indent + 1, false, Modifier.STATIC | Modifier.FINAL);
            return;
        }

        printField(field.getName(), value, terminal, indent,
                value != null && value.getClass().isArray() || value instanceof Collection<?> || value instanceof Map, noName
        );

        if (value != null)
        {
            if (value.getClass().isArray())
                for (int i = 0; i < Array.getLength(value); i++)
                {
                    Object element = Array.get(value, i);
                    varDumpRecursive(field, element, terminal, indent + 1, true);
                }
            else if (value instanceof Collection<?>)
                for (Object element : (Collection<?>) value)
                    varDumpRecursive(field, element, terminal, indent + 1, true);
            else if (value instanceof Map)
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet())
                    varDumpRecursive(field, entry, terminal, indent + 1, true);
        }
    }

    private static void varDump(Object o, Terminal terminal, int indent, boolean printFailedMessage, int... ignoreModifiers)
    {
        if (indent > MAX_INDENT)
            return;

        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            if (ArrayUtils.contains(ignoreModifiers, field.getModifiers()))
                continue;

            field.setAccessible(true);

            try
            {
                Object value = field.get(o);

                if (o.equals(value))
                    printString(field.getName(), ChatColor.RED + "Singleton", terminal, indent);

                varDumpRecursive(field, value, terminal, indent, false);
            }
            catch (IllegalAccessException e)
            {
                if (printFailedMessage)
                    printString(field.getName(),
                            ChatColor.RED + "Unable to get the field value: An exception has occurred.", terminal, indent
                    );
                e.printStackTrace();
            }
            catch (Exception e)
            {
                if (e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException"))
                {
                    if (printFailedMessage)
                        printString(field.getName(),
                                ChatColor.RED + "Unable to get the field value: VM security error.", terminal, indent
                        );
                }
                else
                    e.printStackTrace();
            }
        }
    }

    private static void varDump(Object o, Terminal terminal)
    {
        varDump(o, terminal, 0, false);
    }

    public static SignalHandleManager toManager(Terminal terminal)
    {
        SignalHandleManager signalHandleManager = new SignalHandleManager();
        signalHandleManager.register(new DebugSignalHandler(terminal));

        return signalHandleManager;
    }

    @SignalHandler
    public void handleAll(@NotNull InstallProgress<? extends Enum<?>, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>> installProgress, Signal signal)
    {
        if (!(signal instanceof DownloadProgressSignal))
            printSignal(signal, this.terminal);

        handleInputSignals(signal, this.terminal);
    }
}
