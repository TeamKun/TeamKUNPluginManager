package net.kunmc.lab.teamkunpluginmanager.console.commands;

public enum Color
{

    RED("\u001b[00;31m"),
    GREEN("\u001b[00;32m"),
    YELLOW("\u001b[00;33m"),
    PURPLE("\u001b[00;34m"),
    PINK("\u001b[00;35m"),
    CYAN("\u001b[00;36m");

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

}
