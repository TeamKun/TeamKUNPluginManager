package org.kunlab.kpm.signal;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

public class SignalHandlingUtils
{
    public static boolean askContinue(Terminal terminal)
    {
        try
        {
            QuestionResult result = terminal.getInput()
                    .showYNQuestion(LangProvider.get("general.chat.continue"))
                    .waitAndGetResult();
            return result.test(QuestionAttribute.YES);
        }
        catch (InterruptedException e)
        {
            terminal.error(LangProvider.get("general.errors.unknown") + "：%s", e.getMessage());
            return false;
        }
    }
}
