package net.kunmc.lab.kpm.utils;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public abstract class TerminalWriter
{
    private static final int MAX_LENGTH = 50;
    private static final String SEPARATOR =
            ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "=========================================";
    private static final String SEPARATOR_SHORT =
            ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------";
    private static final String KEY_VALUE_FORMAT =
            ChatColor.GREEN + "%s" + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + "%s";

    private static final String KEY_TRUE_FORMAT =
            ChatColor.GREEN + "%s" + ChatColor.WHITE + ": " + ChatColor.GREEN + "はい";
    private static final String KEY_FALSE_FORMAT =
            ChatColor.GREEN + "%s" + ChatColor.WHITE + ": " + ChatColor.RED + "いいえ";

    private static final String KEY_EMPTY_FORMAT =
            ChatColor.GREEN + "%s" + ChatColor.WHITE + ": " + ChatColor.GRAY + "未設定";

    protected final Terminal terminal;

    public abstract void write();

    protected void printString(String key, @NotNull String value)
    {
        value = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (value.length() == MAX_LENGTH)
            value += "…";
        this.printStringFull(key, value);
    }

    protected void printSeparator()
    {
        this.terminal.writeLine(SEPARATOR);
    }

    protected void printSeparatorShort()
    {
        this.terminal.writeLine(SEPARATOR_SHORT);
    }

    protected void printWithFormat(String format, Object... args)
    {
        this.terminal.writeLine(String.format(format, args));
    }

    protected void printBoolean(String key, boolean value)
    {
        this.printWithFormat(value ? KEY_TRUE_FORMAT: KEY_FALSE_FORMAT, key);
    }

    protected void printStringFull(String key, @NotNull String value)
    {
        this.printWithFormat(KEY_VALUE_FORMAT, key, value);
    }

    protected void printStringFull(String key, String value, ClickEvent.Action action, String content)
    {
        this.terminal.write(Component.text(String.format(KEY_VALUE_FORMAT, key, value))
                .clickEvent(ClickEvent.clickEvent(action, content)));
    }

    protected <T> void printStringFull(String key, String value, ClickEvent.Action action, String clickContent,
                                       HoverEvent.Action<T> hoverAction, T hoverContent)
    {
        this.terminal.write(Component.text(String.format(KEY_VALUE_FORMAT, key, value))
                .clickEvent(ClickEvent.clickEvent(action, clickContent))
                .hoverEvent(HoverEvent.hoverEvent(hoverAction, hoverContent)));
    }

    protected void printStringFull(String key, String value, ClickEvent.Action action, String clickContent,
                                   String hoverText)
    {
        this.printStringFull(key, value, action, clickContent,
                HoverEvent.Action.SHOW_TEXT, Component.text(hoverText)
        );
    }

    protected void printStringFull(String key, String value, String clickCommand)
    {
        this.printStringFull(key, value, ClickEvent.Action.RUN_COMMAND, clickCommand);
    }

    protected void printStringFull(String key, String value, String clickCommand, String hoverText)
    {
        this.printStringFull(key, value,
                ClickEvent.Action.RUN_COMMAND, clickCommand,
                ChatColor.AQUA + hoverText
        );
    }

    protected void printEmpty(String key)
    {
        this.printWithFormat(KEY_EMPTY_FORMAT, key);
    }

    protected void printStringOrEmpty(String key, @Nullable String value)
    {
        if (StringUtils.isEmpty(value))
            this.printEmpty(key);
        else if (value.length() <= MAX_LENGTH)
            this.printString(key, value);
        else
            this.printString(key, value.substring(0, MAX_LENGTH) + "…");
    }

    protected void printStringOrEmptyFull(String key, @Nullable String value)
    {
        if (StringUtils.isEmpty(value))
            this.printEmpty(key);
        else
            this.printString(key, value);
    }

    protected void printString(String key, @NotNull String value, ClickEvent.Action action, String content)
    {
        value = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (value.length() == MAX_LENGTH)
            value += "…";
        this.printStringFull(key, value, action, content);
    }

    protected <T> void printString(String key, @NotNull String value, ClickEvent.Action action, String clickContent,
                                   HoverEvent.Action<T> hoverAction, T hoverContent)
    {
        value = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (value.length() == MAX_LENGTH)
            value += "…";
        this.printStringFull(key, value, action, clickContent, hoverAction, hoverContent);
    }

    protected void printString(String key, @NotNull String value, ClickEvent.Action action, String clickContent,
                               String hoverText)
    {
        value = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (value.length() == MAX_LENGTH)
            value += "…";
        this.printStringFull(key, value, action, clickContent, ChatColor.AQUA + hoverText);
    }

    protected void printString(String key, @NotNull String value, String clickCommand)
    {
        value = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (value.length() == MAX_LENGTH)
            value += "…";
        this.printStringFull(key, value, clickCommand);
    }

    protected void printString(String key, @NotNull String value, String clickCommand, String hoverText)
    {
        value = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (value.length() == MAX_LENGTH)
            value += "…";
        this.printStringFull(key, value, clickCommand, ChatColor.AQUA + hoverText);
    }
}
