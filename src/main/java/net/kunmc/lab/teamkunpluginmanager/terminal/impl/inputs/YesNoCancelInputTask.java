package net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs;

import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Input;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class YesNoCancelInputTask extends YesNoInputTask
{
    private static final String[] CANCEL = new String[]{"c", "cancel", "キャンセル"};

    public YesNoCancelInputTask(@NotNull Audience target, @NotNull String question, @NotNull Input input)
    {
        super(target, question, input);
    }

    @Override
    public boolean checkValidInput(String input)
    {
        return super.checkValidInput(input) || ArrayUtils.contains(CANCEL, input);
    }

    public boolean isCancelled()
    {

        return ArrayUtils.contains(CANCEL, getValue());
    }

    public boolean waitAndIsCancelled() throws InterruptedException
    {
        waitForAnswer();
        return isCancelled();
    }

    @Override
    public Map<String, String> getChoices()
    {
        Map<String, String> choices = super.getChoices();
        choices.put("c", "キャンセル");
        return choices;
    }
}
