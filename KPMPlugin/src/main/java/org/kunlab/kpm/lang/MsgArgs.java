package org.kunlab.kpm.lang;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgArgs
{
    private static final Pattern ARG_PATTERN = Pattern.compile("%%([\\w._-]+)%%");
    private final List<Pair<String, String>> args;

    private MsgArgs(List<Pair<String, String>> args)
    {
        this.args = args;
    }

    public static MsgArgs ofEmpty()
    {
        return new MsgArgs(new ArrayList<>());
    }

    public static MsgArgs of(@Language("MCLang") String key, String value)
    {
        return MsgArgs.ofEmpty().add(key, value);
    }

    private static String formatDeep(String msg)
    {
        if (!msg.contains("%%"))
            return msg;

        Matcher matcher = ARG_PATTERN.matcher(msg);
        while (matcher.find())
        {
            String key = matcher.group(1);
            String value = LanguageProvider.get(key);
            msg = msg.replace(matcher.group(), value);
        }

        return formatDeep(msg);
    }

    public MsgArgs add(@Language("MCLang") String key, String value)
    {
        this.args.add(Pair.of(key, value));
        return this;
    }

    public String format(String msg)
    {
        for (Pair<String, String> arg : this.args)
            msg = msg.replace("%%" + arg.getLeft() + "%%", arg.getRight());

        if (!msg.contains("%%"))
            return msg;

        try
        {
            return formatDeep(msg);
        }
        catch (StackOverflowError e)
        {
            return msg;
        }
    }
}
