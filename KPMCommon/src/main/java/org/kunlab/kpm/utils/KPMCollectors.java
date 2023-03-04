package org.kunlab.kpm.utils;

import org.kunlab.kpm.utils.collectors.MappingMapElementCollector;
import org.kunlab.kpm.utils.collectors.MappingPairCollector;
import org.kunlab.kpm.utils.collectors.ReversingCollector;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class KPMCollectors
{
    public static <T> ReversingCollector<T> toReversedList()
    {
        return new ReversingCollector<>();
    }

    public static <L, R, M extends Map<L, R>> MappingPairCollector<L, R, M> toPairMap(Supplier<M> mapSupplier)
    {
        return new MappingPairCollector<>(mapSupplier);
    }

    public static <L, R> MappingPairCollector<L, R, HashMap<L, R>> toPairHashMap()
    {
        return new MappingPairCollector<>(HashMap::new);
    }

    public static <L, R, M extends Map<L, R>> MappingMapElementCollector<L, R, M> toMap(Supplier<M> mapSupplier)
    {
        return new MappingMapElementCollector<>(mapSupplier);
    }

    public static <L, R> MappingMapElementCollector<L, R, HashMap<L, R>> toHashMap()
    {
        return new MappingMapElementCollector<>(HashMap::new);
    }
}
