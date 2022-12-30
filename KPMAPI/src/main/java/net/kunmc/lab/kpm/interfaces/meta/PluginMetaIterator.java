package net.kunmc.lab.kpm.interfaces.meta;

import net.kunmc.lab.kpm.meta.PluginMeta;

import java.util.Iterator;

public interface PluginMetaIterator extends Iterator<PluginMeta>, AutoCloseable
{
    @Override
    boolean hasNext();

    @Override
    PluginMeta next();

    @Override
    void close();

    @Override
    void remove();
}
