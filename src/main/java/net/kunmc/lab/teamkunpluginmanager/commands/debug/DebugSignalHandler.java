package net.kunmc.lab.teamkunpluginmanager.commands.debug;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Question;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.install.signals.AlreadyInstalledPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class DebugSignalHandler implements InstallerSignalHandler
{
    private final Terminal terminal;

    private static <T extends InstallerSignal> void handleInputSignals(T signal, Terminal terminal)
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
                ignoredPluginSignal.setCancelInstall(question.waitAndGetResult().test(QuestionAttribute.YES));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static <T extends InstallerSignal> void printSignal(T signal, Terminal terminal)
    {
        terminal.writeLine("====================");
        terminal.info("On Signal: " + signal.getClass().getSimpleName());
        terminal.info("Values:");
        varDump(signal, terminal);
        terminal.writeLine("====================");
    }

    private static void printField(String fieldName, Object o, Terminal terminal, int indent, boolean noValue)
    {
        String typePrefix = getTypePrefix(o);

        String indentStr = StringUtils.repeat("  ", indent);

        if (noValue)
            terminal.writeLine(indentStr + ChatColor.YELLOW + fieldName + " ==> " + typePrefix);
        else if (o instanceof Map.Entry)
        {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            terminal.writeLine(indentStr + ChatColor.YELLOW + fieldName + " ==> " +
                    typePrefix + " " + entry.getKey() + ": " + entry.getValue());
        }
        else
            terminal.writeLine(indentStr + ChatColor.YELLOW + fieldName + " ==> " + typePrefix + o);
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
                clazz.isAssignableFrom(String.class) ||
                clazz.isAssignableFrom(Integer.class) ||
                clazz.isAssignableFrom(Boolean.class) ||
                clazz.isAssignableFrom(Double.class) ||
                clazz.isAssignableFrom(Float.class) ||
                clazz.isAssignableFrom(Long.class) ||
                clazz.isAssignableFrom(Short.class) ||
                clazz.isAssignableFrom(Byte.class) ||
                clazz.isAssignableFrom(Character.class) ||
                clazz.isArray() ||
                clazz.isEnum() ||
                clazz.isAssignableFrom(Map.class) ||
                clazz.isAssignableFrom(Collection.class);
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
        else if (Map.Entry.class.isAssignableFrom(clazz))
            return prefix + getTypePrefix(clazz.getGenericSuperclass()) + " => " +
                    getTypePrefix(clazz.getGenericInterfaces()[0]);
        else
            return prefix + "L" + clazz.getName() + ";: ";
    }

    private static void varDumpRecursive(Field field, Object value, Terminal terminal, int indent)
            throws IllegalAccessException
    {
        if (!isCompatible(value))
        {
            printField(field.getName(), value, terminal, indent, false);
            varDump(value, terminal, indent + 1, false);
            return;
        }

        printField(field.getName(), value, terminal, indent,
                value != null && value.getClass().isArray() || value instanceof Collection<?> || value instanceof Map
        );

        if (value != null)
        {
            if (value.getClass().isArray())
                for (int i = 0; i < Array.getLength(value); i++)
                {
                    Object element = Array.get(value, i);
                    varDumpRecursive(field, element, terminal, indent + 1);
                }
            else if (value instanceof Collection<?>)
                for (Object element : (Collection<?>) value)
                    varDumpRecursive(field, element, terminal, indent + 1);
            else if (value instanceof Map)
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet())
                    varDumpRecursive(field, entry, terminal, indent + 1);
        }
    }

    private static void varDump(Object o, Terminal terminal, int indent, boolean printFailedMessage)
    {
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            field.setAccessible(true);

            try
            {
                Object value = field.get(o);

                if (o.equals(value))
                    printString(field.getName(), ChatColor.RED + "Singleton", terminal, indent);

                varDumpRecursive(field, value, terminal, indent);
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

    @Override
    public <T extends InstallerSignal> void handleSignal(@NotNull InstallProgress<?> installProgress, @NotNull T signal)
    {
        printSignal(signal, terminal);

        handleInputSignals(signal, terminal);
    }
}