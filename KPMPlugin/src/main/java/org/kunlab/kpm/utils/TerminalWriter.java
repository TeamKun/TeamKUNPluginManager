package org.kunlab.kpm.utils;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
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
    protected final Terminal terminal;

    public abstract void write();

    protected void printString(String key, @NotNull String value)
    {
        String croppedValue = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (croppedValue.length() == MAX_LENGTH)
            croppedValue += "…";
        this.printStringFull(key, croppedValue);
    }

    protected void printSeparator()
    {
        this.terminal.writeLine(SEPARATOR);
    }

    protected void printSeparatorShort()
    {
        this.terminal.writeLine(SEPARATOR_SHORT);
    }


    protected void printBoolean(String key, boolean value)
    {
        String localeKey = value ?
                "general.chat.writer.key_yes":
                "general.chat.writer.key_no";

        this.terminal.writeLine(LangProvider.get(localeKey, MsgArgs.of("key", "%%" + key + "%%")));
    }

    protected void printStringFull(String key, @NotNull String value)
    {
        this.terminal.writeLine(LangProvider.get(
                        "general.chat.writer.key_value",
                        MsgArgs.of("key", "%%" + key + "%%").add("value", value)
                )
        );
    }

    protected void printStringFull(String key, String value, ClickEvent.Action action, String content)
    {
        this.terminal.write(
                Text.of(
                        LangProvider.get(
                                "general.chat.writer.key_value",
                                MsgArgs.of("key", "%%" + key + "%%").add("value", value)
                        )
                ).clickAction(action, content)
        );
    }

    protected void printStringFull(String key, String value, ClickEvent.Action clickAction, String clickContent,
                                   HoverEvent.Action hoverAction, Text hoverContent)
    {
        this.terminal.write(Text.of(LangProvider.get(
                                        "general.chat.writer.key_value",
                                        MsgArgs.of("key", "%%" + key + "%%").add("value", value)
                                )
                        ).clickAction(clickAction, clickContent)
                        .hoverAction(hoverAction, hoverContent)
        );
    }

    protected void printStringFull(String key, String value, ClickEvent.Action action, String clickContent,
                                   String hoverText)
    {
        this.printStringFull(key, value, action, clickContent,
                HoverEvent.Action.SHOW_TEXT, Text.of(hoverText)
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
        this.terminal.writeLine(LangProvider.get(
                "general.chat.writer.key_empty",
                MsgArgs.of("key", "%%" + key + "%%")
        ));
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
        String croppedValue = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (croppedValue.length() == MAX_LENGTH)
            croppedValue += "…";
        this.printStringFull(key, croppedValue, action, content);
    }

    protected <T> void printString(String key, @NotNull String value, ClickEvent.Action action, String clickContent,
                                   HoverEvent.Action hoverAction, Text hoverContent)
    {
        String croppedValue = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (croppedValue.length() == MAX_LENGTH)
            croppedValue += "…";
        this.printStringFull(key, croppedValue, action, clickContent, hoverAction, hoverContent);
    }

    protected void printString(String key, @NotNull String value, ClickEvent.Action action, String clickContent,
                               String hoverText)
    {
        String croppedValue = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (croppedValue.length() == MAX_LENGTH)
            croppedValue += "…";
        this.printStringFull(key, croppedValue, action, clickContent, ChatColor.AQUA + hoverText);
    }

    protected void printString(String key, @NotNull String value, String clickCommand)
    {
        String croppedValue = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (croppedValue.length() == MAX_LENGTH)
            croppedValue += "…";
        this.printStringFull(key, croppedValue, clickCommand);
    }

    protected void printString(String key, @NotNull String value, String clickCommand, String hoverText)
    {
        String croppedValue = value.substring(0, Math.min(value.length(), MAX_LENGTH));
        if (croppedValue.length() == MAX_LENGTH)
            croppedValue += "…";
        this.printStringFull(key, croppedValue, clickCommand, ChatColor.AQUA + hoverText);
    }
}
