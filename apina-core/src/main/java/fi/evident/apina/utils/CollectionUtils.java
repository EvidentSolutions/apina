package fi.evident.apina.utils;

import java.util.Collection;
import java.util.List;
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
}
