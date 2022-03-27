package net.kunmc.lab.teamkunpluginmanager.terminal.impl.inputs;

import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Input;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.AbstractInputTask;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class YesNoInputTask extends AbstractInputTask
{
    protected static final String[] YES = new String[]{"y", "yes", "はい"};
    protected static final String[] NO = new String[]{"n", "no", "いいえ"};

    public YesNoInputTask(@NotNull Audience target, @NotNull String question, @NotNull Input input)
    {
        super(target, question, input);
    }

    @Override
    public boolean checkValidInput(String input)
    {
        return ArrayUtils.contains(YES, input.toLowerCase()) ||
                ArrayUtils.contains(NO, input.toLowerCase());
    }

    @Override
    public Map<String, String> getChoices()
    {
        return new HashMap<String, String>()
        {{
            put("yes", "はい");
            put("no", "いいえ");
        }};
    }

    /**
     * スレッドをブロックして真偽値として値を取得します。
     *
     * @return 真偽値
     * @throws InterruptedException キャンセルされた場合
     */
    public boolean waitAndGetValueAsBoolean() throws InterruptedException
    {
        return ArrayUtils.contains(YES, waitAndGetValue().toLowerCase());
    }

    /**
     * 真偽値として値を取得します。
     *
     * @return 真偽値
     */
    public boolean getValueAsBoolean()
    {
        return ArrayUtils.contains(YES, getValue().toLowerCase());
    }
}
