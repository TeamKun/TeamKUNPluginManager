package net.kunmc.lab.teamkunpluginmanager.utils;

public class Pair<L, R>
{

    private final L left;
    private final R right;

    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }

    public L getKey()
    {
        return left;
    }

    public R getValue()
    {
        return right;
    }
}
