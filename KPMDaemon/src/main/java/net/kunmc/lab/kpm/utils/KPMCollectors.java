package net.kunmc.lab.kpm.utils;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class KPMCollectors
{
    public static <T> ReversedCollector<T> toReversedList()
    {
        return new ReversedCollector<>();
    }

    public static <L, R, M extends Map<L, R>> PairMapper<L, R, M> toPairMap(Supplier<M> mapSupplier)
    {
        return new PairMapper<>(mapSupplier);
    }

    public static <L, R> PairMapper<L, R, HashMap<L, R>> toPairHashMap()
    {
        return new PairMapper<>(HashMap::new);
    }

    private static class ReversedCollector<T> implements Collector<T, List<T>, List<T>>
    {
        @Override
        public Supplier<List<T>> supplier()
        {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<T>, T> accumulator()
        {
            return (list, t) -> list.add(0, t);
        }

        @Override
        public BinaryOperator<List<T>> combiner()
        {
            return (list1, list2) ->
            {
                list1.addAll(0, list2);
                return list1;
            };
        }

        @Override
        public Function<List<T>, List<T>> finisher()
        {
            return list -> list;
        }

        @Override
        public Set<Characteristics> characteristics()
        {
            return EnumSet.of(Characteristics.CONCURRENT);
        }
    }

    // pair to hashmap
    @AllArgsConstructor
    private static class PairMapper<L, R, M extends Map<L, R>> implements Collector<Pair<L, R>, M, M>
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
}
