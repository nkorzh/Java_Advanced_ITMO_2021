package info.kgeorgiy.ja.kozhuharov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final EasyReverseList<T> list;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        NavigableSet<T> set = new TreeSet<>(comparator);
        set.addAll(collection);
        this.list = new EasyReverseList<>(set);
        this.comparator = comparator;
    }

    private ArraySet(EasyReverseList<T> arrayList, Comparator<? super T> comparator) {
        this.list = arrayList;
        this.comparator = comparator;
    }

    /***
     * Subsets
     ***/

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new EasyReverseList<>(list, true), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return getSubSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    private NavigableSet<T> getSubSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        final int fromIndex = upperIndex(fromElement, fromInclusive);
        final int toIndex = lowerIndex(toElement, toInclusive);

        return fromIndex > toIndex ? new ArraySet<>(comparator) :
                new ArraySet<>(list.subList(fromIndex, toIndex + 1), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return getSubSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return getSubSet(fromElement, inclusive, last(), true);
    }

    /***
     * Indexes and searching
     ***/

    private int lowerIndex(final T element, boolean inclusive) {
        final int index = search(element);
        return index < 0 ? -index - 2 :
                (inclusive ? index : index - 1);
    }

    private int upperIndex(final T element, boolean inclusive) {
        final int index = search(element);
        return index < 0 ? -index - 1 :
                (inclusive ? index : index + 1);
    }

    private boolean isCorrectIndex(int index) {
        return 0 <= index && index < size();
    }

    private int search(final T object) {
        return Collections.binarySearch(list, object, comparator);
    }

    /***
     * Element access
     ***/

    private T getOrNull(int index) {
        return isCorrectIndex(index) ? list.get(index) : null;
    }

    private T getOrFail(int index) {
        if (!isEmpty()) {
            return list.get(index);
        }
        throw new NoSuchElementException();
    }

    @Override
    public T first() {
        return getOrFail(0);
    }

    @Override
    public T last() {
        return getOrFail(size() - 1);
    }

    @Override
    public T lower(T t) {
        return getOrNull(lowerIndex(t, false));
    }

    @Override
    public T floor(T t) {
        return getOrNull(lowerIndex(t, true));
    }

    @Override
    public T ceiling(T t) {
        return getOrNull(upperIndex(t, true));
    }

    @Override
    public T higher(T t) {
        return getOrNull(upperIndex(t, false));
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }


    @Override
    public boolean contains(Object o) {
        return search((T) o)>= 0;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    private int compare(T a, T b) {
        return comparator == null ? ((Comparable<? super T>) a).compareTo(b)
                : comparator.compare(a, b);
    }

    /***
     * Unsupported methods
     ***/

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }
}
