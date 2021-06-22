package info.kgeorgiy.ja.kozhuharov.i18n.interfaces;

import java.util.function.Consumer;

public interface TextStatisticsCounter<T, E> extends Consumer<T> {

    long getCount();

    long getCountDistinct();

    T getMinEntry();

    T getMaxEntry();

    E getAverage(); // length or value
}
