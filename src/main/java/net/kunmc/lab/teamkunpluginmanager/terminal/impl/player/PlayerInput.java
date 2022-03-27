package net.kunmc.lab.teamkunpluginmanager.terminal.impl.player;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Input;
import net.kunmc.lab.teamkunpluginmanager.terminal.framework.InputTask;
import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Terminal;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.InputManager;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs.BasicStringInputTask;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs.ChoiceInputTask;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs.YesNoCancelInputTask;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs.YesNoInputTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class PlayerInput implements Input
{
    @Getter
    private final Terminal terminal;

    public PlayerInput(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private UUID getPlayerUUID()
    {
        return ((Player) terminal.getAudience()).getUniqueId();
    }

    private InputTask registerInputTask(@NotNull InputTask task)
    {
        InputManager.getInstance().addInputTask(getPlayerUUID(), task);
        return task;
    }

    @Override
    public @NotNull InputTask showYNQuestion(@NotNull String question)
    {
        return registerInputTask(new YesNoInputTask(terminal.getAudience(), question, this));
    }

    @Override
    public @NotNull InputTask showYNQuestionCancellable(@NotNull String question)
    {
        return registerInputTask(new YesNoCancelInputTask(terminal.getAudience(), question, this));
    }

    @Override
    public @NotNull InputTask showInputQuestion(@NotNull String question)
    {
        return registerInputTask(new BasicStringInputTask(terminal.getAudience(), question, this));
    }

    @Override
    public @NotNull InputTask showChoiceQuestion(@NotNull String question, String... choices)
    {
        return registerInputTask(new ChoiceInputTask(terminal.getAudience(), question, this, choices));
    }

    @Override
    public @NotNull InputTask showChoiceQuestion(@NotNull String question, @NotNull HashMap<String, String> choices)
    {
        return registerInputTask(new ChoiceInputTask(terminal.getAudience(), question, this, choices));
    }

    @Override
    public void cancelQuestion(InputTask task)
    {
        InputManager.getInstance().cancelInputTask(getPlayerUUID(), task);
    }
}
