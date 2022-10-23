package net.kunmc.lab.kpm.signal;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

public class SignalHandlingUtils
{
    public static boolean askContinue(Terminal terminal)
    {
        try
        {
            QuestionResult result = terminal.getInput().showYNQuestion("続行しますか?").waitAndGetResult();
            return result.test(QuestionAttribute.YES);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました: " + e.getMessage());
            return false;
        }
    }
}
