package info.kgeorgiy.ja.kozhuharov.i18n;

import info.kgeorgiy.ja.kozhuharov.i18n.interfaces.TextStatisticsCounter;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class TextStatisticsCounterAbstract<T extends Comparable<? super T>, E> implements TextStatisticsCounter<T, E> {
    protected final Locale locale;
    protected Map<T, Integer> units;
    protected long count;
    protected long countDistinct;
    protected int minLength;
    protected int maxLength;
    protected long sumLength;
    protected T minUnit;
    protected T maxUnit;

    public TextStatisticsCounterAbstract(Locale locale) {
        this.locale = locale;
        this.units = new HashMap<>();
    }

    @Override
    public void accept(T t) {
        int newAmount = units.merge(t, 1, Integer::sum);
        count++;
        if (newAmount == 1) {
            countDistinct++;
        }
        if (minUnit == null || t.compareTo(minUnit) < 0) {
            minUnit = t;
        }
        if (maxUnit == null || maxUnit.compareTo(t) < 0) {
            maxUnit = t;
        }
        int length = getLength(t.toString());
        if (minLength == 0 || length < minLength) {
            updateMinLength(t, length);
        } else if (length > maxLength) {
            updateMaxLength(t, length);
        }
        sumLength += length;
    }

    protected int getLength(String t) {
        final BreakIterator it = BreakIterator.getCharacterInstance(locale);
        it.setText(t);

        int index = it.first();
        int cntIters = 0;
        while (index != BreakIterator.DONE) {
            cntIters++;
            index = it.next();
        }

        return cntIters - 1;
    }

    protected void updateMinLength(T t, int length) {
        minLength = length;
    }

    protected void updateMaxLength(T t, int length) {
        maxLength = length;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public long getCountDistinct() {
        return countDistinct;
    }

    @Override
    public T getMinEntry() {
        return minUnit;
    }

    @Override
    public T getMaxEntry() {
        return maxUnit;
    }
}
