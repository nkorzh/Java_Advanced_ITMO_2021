package info.kgeorgiy.ja.kozhuharov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private <T, U> U distribute(final int threads,
                                final List<? extends T> values,
                                final Function<Stream<? extends T>, ? extends U> calcFunc,
                                final Function<Stream<? extends U>, ? extends U> collectFunc)
            throws InterruptedException {
        if (threads < 1) {
            throw new IllegalArgumentException("Invalid thread amount: " + threads);
        }
        final int threadsAmount = Math.min(threads, values.size());
        final int partSize = values.size() / threadsAmount;
        final int restSize = values.size() % threadsAmount;

        final Function<Integer, List<? extends T>> getThreadPart = threadNumber -> {
            final int leftIndex = threadNumber * partSize;
            final int rightIndex = leftIndex + partSize + (threadNumber == threadsAmount - 1 ? restSize : 0);
            return values.subList(leftIndex, rightIndex);
        };
        final List<Thread> threadList = new ArrayList<>();
        final List<U> result = new ArrayList<>(Collections.nCopies(threadsAmount, null));
        for (int i = 0; i < threadsAmount; i++) {
            final int threadNum = i;
            final Thread thread = new Thread(() ->
                    result.set(
                            threadNum,
                            calcFunc.apply(getThreadPart.apply(threadNum).stream()))
            );
            threadList.add(thread);
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        return collectFunc.apply(result.stream());
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
            throws InterruptedException {
        final Function<Stream<? extends T>, ? extends T> maxFunc =
                stream -> stream.max(comparator)
                        .orElse(null);
        return distribute(threads, values, maxFunc, maxFunc);
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
            throws InterruptedException {
        final Function<Stream<? extends T>, ? extends T> minFunc =
                stream -> stream.min(comparator)
                        .orElse(null);
        return distribute(threads, values, minFunc, minFunc);
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return distribute(
                threads,
                values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue)
        );
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    /// Medium

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return distribute(
                threads,
                values,
                stream -> stream.map(Object::toString)
                        .collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining())
        );
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return distribute(
                threads,
                values,
                stream -> stream.filter(predicate)
                        .collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values,
                              final Function<? super T, ? extends U> f) throws InterruptedException {
        return distribute(
                threads,
                values,
                stream -> stream.map(f).
                        collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }
}
