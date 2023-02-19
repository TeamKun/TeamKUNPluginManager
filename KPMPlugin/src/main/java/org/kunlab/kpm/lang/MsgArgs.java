package org.kunlab.kpm.lang;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static MsgArgs of(String key, Object value)
    {
        return MsgArgs.ofEmpty().add(key, String.valueOf(value));
    }

    private static String formatColors(String msg)
    {
        // This part is hideous, but I don't know how to make it better without any performance loss.
        // Your contribution is welcome, so please make a pull request if you have any idea.
        // @formatter:off
        return StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
        StringUtils.replace(
            msg,
            "%%black%%", ChatColor.BLACK.toString()),
            "%%dark_blue%%", ChatColor.DARK_BLUE.toString()),
            "%%dark_green%%", ChatColor.DARK_GREEN.toString()),
            "%%dark_aqua%%", ChatColor.DARK_AQUA.toString()),
            "%%dark_red%%", ChatColor.DARK_RED.toString()),
            "%%dark_purple%%", ChatColor.DARK_PURPLE.toString()),
            "%%gold%%", ChatColor.GOLD.toString()),
            "%%gray%%", ChatColor.GRAY.toString()),
            "%%dark_gray%%", ChatColor.DARK_GRAY.toString()),
            "%%blue%%", ChatColor.BLUE.toString()),
            "%%green%%", ChatColor.GREEN.toString()),
            "%%aqua%%", ChatColor.AQUA.toString()),
            "%%red%%", ChatColor.RED.toString()),
            "%%light_purple%%", ChatColor.LIGHT_PURPLE.toString()),
            "%%yellow%%", ChatColor.YELLOW.toString()),
            "%%white%%", ChatColor.WHITE.toString()),
            "%%reset%%", ChatColor.RESET.toString()),
            "%%bold%%", ChatColor.BOLD.toString()),
            "%%strikethrough%%", ChatColor.STRIKETHROUGH.toString()),
            "%%underline%%", ChatColor.UNDERLINE.toString()),
            "%%italic%%", ChatColor.ITALIC.toString()),
            "%%magic%%", ChatColor.MAGIC.toString()),
            "%%obfuscated%%", ChatColor.MAGIC.toString()
        );
        // @formatter:on
    }

    private static String replaceGroup(String input, Matcher matcher, String replacement)
    {
        int start = matcher.start();
        int end = matcher.end();
        return input.substring(0, start) +
                replacement +
                input.substring(end);
    }

    private String formatDeep(String msg)
    {
        Map<String, String> argMap = new HashMap<>();
        Matcher matcher = ARG_PATTERN.matcher(msg);
        while (matcher.find())
        {
            String key = matcher.group(1);
            String value = argMap.get(key);
            if (value == null)
            {
                value = LangProvider.get(key, this);
                if (value == null)
                    value = "%%" + key + "%%";
                argMap.put(key, value);
            }
            msg = replaceGroup(msg, matcher, value);
        }
        return msg;
    }

    public MsgArgs add(String key, Object value)
    {
        this.args.add(Pair.of(key, String.valueOf(value)));
        return this;
    }

    public MsgArgs add(MsgArgs args)
    {
        this.args.addAll(args.args);
        return this;
    }

    public String format(String msg)
    {
        for (Pair<String, String> arg : this.args)
            msg = msg.replace("%%" + arg.getLeft() + "%%", arg.getRight());

        msg = formatColors(msg);

        if (!msg.contains("%%"))
            return msg;

        try
        {
            return this.formatDeep(msg);
        }
        catch (StackOverflowError e)
        {
            return msg;
        }
    }
}
