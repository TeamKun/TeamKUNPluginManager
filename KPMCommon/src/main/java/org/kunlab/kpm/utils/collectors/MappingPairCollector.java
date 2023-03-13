package org.kunlab.kpm.utils.collectors;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@AllArgsConstructor
public class MappingPairCollector<L, R, M extends Map<L, R>> implements Collector<Pair<L, R>, M, M>
{
    private final Supplier<M> supplier;

    @Override
    public Supplier<M> supplier()
    {
        return this.supplier;
    }

    @Override
    public BiConsumer<M, Pair<L, R>> accumulator()
    {
        return (map, pair) -> map.put(pair.getLeft(), pair.getRight());
    }

    @Override
    public BinaryOperator<M> combiner()
    {
        return (map1, map2) ->
        {
            map1.putAll(map2);
            return map1;
        };
    }

    @Override
    public Function<M, M> finisher()
    {
        return map -> map;
    }

    @Override
    public Set<Characteristics> characteristics()
    {
        return EnumSet.of(Characteristics.CONCURRENT);
    }
}
