package net.kunmc.lab.teamkunpluginmanager.console.utils;

import org.fusesource.jansi.Ansi;

public enum Color
{

    RED("@|red "),
    GREEN("@|green "),
    YELLOW("@|yellow "),
    MAGENTA("@|magenta "),
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
