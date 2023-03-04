package org.kunlab.kpm.utils.collectors;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ReversingCollector<T> implements Collector<T, List<T>, List<T>>
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
