package org.kunlab.kpm.meta.interfaces;

import org.kunlab.kpm.meta.PluginMeta;

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
