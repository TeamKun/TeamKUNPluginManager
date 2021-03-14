package net.kunmc.lab.teamkunpluginmanager.console.utils;

import net.kunmc.lab.teamkunpluginmanager.console.PluginManagerConsole;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class CommandUtil
{
    public static boolean containsIgnoreCase(String[] target, String maf)
    {
        return Arrays.stream(target).parallel().anyMatch(maf::equalsIgnoreCase);
    }

    public static String genHelp(String name, String description, String[] aliases, Parameter[] param, String[] examples)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("使用法： java -jar ").append(PluginManagerConsole.classPath).append(" ").append(name).append(" ");

        Arrays.stream(param).forEach(parameter -> {
            builder.append(String.format(
                    "%s%s%s",
                    parameter.require ? "<": "[",
                    parameter.name,
                    parameter.require ? ">": "]"
            ));
        });

        builder.append("\n\n");

        builder.append(description);
        builder.append("\n\n");

        builder.append("パラメータ：");
        builder.append("\n");
        Arrays.stream(param).parallel().forEach(parameter -> {
            builder.append("    ");
            builder.append(parameter.name).append("：").append(parameter.description);
            builder.append("\n");
            builder.append("        ");
            if (parameter.usage != null)
                builder.append("指定方法：").append(parameter.usage);
            builder.append("\n");
        });

        builder.append("\n");

        builder.append("エイリアス： ").append(String.join(", ", aliases)).append("\n");
        builder.append("例：");
        builder.append("\n");

        AtomicInteger aliasCount = new AtomicInteger();
        Arrays.stream(examples).forEach(s -> {
            builder.append("    ... ");

            if (aliases.length == 0 || aliasCount.get() == 0)
            {
                builder.append(name);
                aliasCount.incrementAndGet();
            }
            else if (aliasCount.get() < aliases.length)
                builder.append(aliases[aliasCount.getAndIncrement()]);
            else
                builder.append(new Random().nextBoolean() ? name: aliases[new Random().nextInt(aliases.length)]);

            builder.append(" ");
            builder.append(s);
            builder.append("\n");
        });

        aliasCount.set(aliases.length - aliasCount.get());

        if (aliasCount.get() > 0)
        {
            IntStream.range(0, aliasCount.incrementAndGet())
                    .forEach(value -> {
                        builder.append("    ... ");
                        builder.append(aliases[value]);
                        builder.append(" ");
                        builder.append(examples[new Random().nextInt(examples.length)]);
                        builder.append("\n");
                    });
        }

        return builder.toString();
    }

    public static class Parameter
    {
        public String name;
        public String description;
        public String usage;
        public boolean require;

        public Parameter(String name, String description, String usage, boolean require)
        {
            this.name = name;
            this.description = description;
            this.usage = usage;
            this.require = require;
        }
    }

}
