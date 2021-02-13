package net.kunmc.lab.teamkunpluginmanager.console.commands;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public enum Color
{

    RED("@|red "),
    GREEN("@|green "),
    YELLOW("@|yellow "),
    PURPLE("@|purple "),
    PINK("@|pink "),
    CYAN("@|cyan ");

    String code;

    Color(String code)
    {
        this.code = code;
    }

    @Override
    public String toString()
    {
        return code;
    }

    public Ansi format(String txt)
    {
        return Ansi.ansi().eraseLine().render(code + (txt.endsWith("|@") ? txt: txt + "|@"));
    }
}
