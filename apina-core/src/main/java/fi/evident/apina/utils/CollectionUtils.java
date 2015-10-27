package fi.evident.apina.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public final class CollectionUtils {

    public static <A,B> List<B> map(Collection<? extends A> xs, Function<? super A, ? extends B> mapper) {
        return xs.stream().map(mapper).collect(toList());
    }

    public static <A,B> List<B> map(A[] xs, Function<? super A, ? extends B> mapper) {
        return Stream.of(xs).map(mapper).collect(toList());
    }

    public static <T> List<T> filter(Collection<? extends T> xs, Predicate<? super T> predicate) {
        return xs.stream().filter(predicate).collect(toList());
    }

    public static String join(Collection<?> objects, String delimiter) {
        return objects.stream().map(String::valueOf).collect(joining(delimiter));
    }

    public static String join(Collection<?> objects, String delimiter, String prefix, String suffix) {
        return objects.stream().map(String::valueOf).collect(joining(delimiter, prefix, suffix));
    }

    public static String join(Object[] objects, String delimiter, String prefix, String suffix) {
        return join(asList(objects), delimiter, prefix, suffix);
    }

    public static <T> List<T> filterByType(Collection<? super T> objects, Class<T> type) {
        return objects.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(toList());
    }

    public static <T> Stream<T> optionalToStream(Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    public static <T> List<T> concat(Collection<? extends T> xs, Collection<? extends T> ys) {
        ArrayList<T> result = new ArrayList<>(xs.size() + ys.size());
        result.addAll(xs);
        result.addAll(ys);
        return result;
    }

    public static <T> List<T> cons(T x, Collection<? extends T> xs) {
        ArrayList<T> result = new ArrayList<>(xs.size() + 1);
        result.add(x);
        result.addAll(xs);
        return result;
    }

    public static <T> boolean hasDuplicates(Collection<? extends T> xs) {
        HashSet<T> ys = new HashSet<>(xs.size());

        for (T x : xs)
            if (!ys.add(x))
                return true;

        return false;
    }
}
