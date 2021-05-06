package info.kgeorgiy.ja.kozhuharov.arrayset;

import java.util.*;

public class EasyReverseList<T> extends AbstractList<T> implements RandomAccess {
    private final List<T> list;
    private final boolean isReversed;

    private EasyReverseList(final List<T> list) {
        this.list = Collections.unmodifiableList(list);
        this.isReversed = false;
    }

    public EasyReverseList(final Collection<T> collection) {
        this.list = List.copyOf(collection);
        this.isReversed = false;
    }

    public EasyReverseList(final EasyReverseList<T> easyReverseList, final boolean reverseNewList) {
        this.list = easyReverseList.list;
        this.isReversed = easyReverseList.isReversed ^ reverseNewList;
    }

    @Override
    public T get(int index) {
        return list.get(correctIndex(index));
    }

    @Override
    public EasyReverseList<T> subList(int fromIndex, int toIndex) {
        return isReversed ?
                new EasyReverseList<>(list.subList(reverseIndex(toIndex - 1), reverseIndex(fromIndex) + 1)) :
                new EasyReverseList<>(list.subList(fromIndex, toIndex));
    }

    @Override
    public int size() {
        return list.size();
    }

    private int reverseIndex(int index) {
        return list.size() - index - 1;
    }

    private int correctIndex(int index) {
        return isReversed ? reverseIndex(index) : index;
    }
}
