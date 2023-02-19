package org.kunlab.kpm.signal;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.lang.LangProvider;

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
            e.printStackTrace();
            terminal.error(LangProvider.get("general.errors.unknown") + "：%s", e.getMessage());
            return false;
        }
    }
}
