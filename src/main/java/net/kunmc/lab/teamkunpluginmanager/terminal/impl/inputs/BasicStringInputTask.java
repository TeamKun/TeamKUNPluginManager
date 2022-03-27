package net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs;

import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Input;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.AbstractInputTask;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 標準的なテキスト入力用のタスク
 */
public class BasicStringInputTask extends AbstractInputTask
{

    public BasicStringInputTask(@NotNull Audience target, @NotNull String question, @NotNull Input input)
    {
        super(target, question, input);
    }

    @Override
    public boolean checkValidInput(String input)
    {
        return true;
    }

    @Override
    public @Nullable Map<String, String> getChoices()
    {
        return null;
    }
}
