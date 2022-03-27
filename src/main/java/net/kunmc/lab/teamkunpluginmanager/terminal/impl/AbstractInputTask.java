package net.kunmc.lab.teamkunpluginmanager.terminal.impl;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Input;
import net.kunmc.lab.teamkunpluginmanager.terminal.framework.InputTask;
import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Terminal;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs.BasicStringInputTask;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 入力のタスク
 */
@Getter
@EqualsAndHashCode
public abstract class AbstractInputTask implements InputTask
{
    private final UUID uuid;
    private final UUID target;
    private final Input input;
    private final String question;

    @Getter(AccessLevel.PRIVATE)
    private final Object locker;

    private String value;
    @Getter(AccessLevel.PRIVATE)
    private boolean valuePresent;

    public AbstractInputTask(@NotNull Audience target, @NotNull String question, @NotNull Input input)
    {
        this.uuid = UUID.randomUUID();
        this.target = target instanceof Player ? ((Player) target).getUniqueId(): null;
        this.input = input;
        this.question = question;

        this.locker = new Object();

        this.value = null;
        this.valuePresent = false;
    }

    @Override
    public void setValue(@NotNull String value)
    {
        if (valuePresent)
            throw new IllegalStateException("value is already set");

        this.valuePresent = true;
        this.value = value;
        synchronized (locker)
        {
            locker.notifyAll();
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public @NotNull String waitAndGetValue() throws InterruptedException
    {
        waitForAnswer();
        return getRawValue();
    }

    @Override
    public void waitForAnswer() throws InterruptedException
    {
        if (valuePresent)
            return;

        synchronized (locker)
        {
            locker.wait();
        }
    }

    @Override
    public @Nullable String getRawValue()
    {
        return value;
    }

    @Override
    public boolean isValueAvailable()
    {
        return valuePresent;
    }

    @Override
    public void cancel()
    {
        if (valuePresent)
            return;
        synchronized (locker)
        {
            locker.notifyAll();
        }
    }

    private void printSeparator(Terminal terminal)
    {
        terminal.writeLine(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "================================================");
    }

    private void printChoices(Terminal terminal, Map<String, String> choices)
    {
        AtomicInteger index = new AtomicInteger(1);
        choices.forEach((value, text) -> terminal.write(
                Component.text(ChatColor.YELLOW.toString() + index.getAndIncrement() +
                                " " + ChatColor.GREEN + text + "( " + value + " )")
                        .clickEvent(ClickEvent.suggestCommand(value))
                        .hoverEvent(HoverEvent.showText(
                                Component.text(ChatColor.YELLOW + "クリックして補完！")))
        ));
    }

    @Override
    public void printQuestion()
    {
        Terminal terminal = input.getTerminal();

        printSeparator(terminal);
        terminal.writeLine(ChatColor.GREEN + "    " + question);

        if (this instanceof BasicStringInputTask)
        {
            terminal.writeLine("    " + ChatColor.GREEN + "回答をチャットまたはコンソールに入力してください。");
            printSeparator(terminal);
            return;
        }

        Map<String, String> choices = getChoices();
        if (choices != null)
            printChoices(terminal, choices);

        printSeparator(terminal);
    }
}
