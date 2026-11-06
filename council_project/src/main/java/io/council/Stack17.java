package io.council;

import java.util.EmptyStackException;

public class Stack17<T extends Comparable<T>> {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] data;
    private int size;
    private Object[] minStack;
    private int minSize;
    private Object[] maxStack;
    private int maxSize;

    public Stack17() {
        this(DEFAULT_CAPACITY);
    }

    public Stack17(int initialCapacity) {
        if (initialCapacity < 1) throw new IllegalArgumentException("Initial capacity must be positive");
        data = new Object[initialCapacity];
        minStack = new Object[initialCapacity];
        maxStack = new Object[initialCapacity];
        size = 0;
        minSize = 0;
        maxSize = 0;
    }

    public void push(T value) {
        if (value == null) throw new IllegalArgumentException("Null values not supported");
        ensureCapacity();
        data[size++] = value;

        if (minSize == 0 || value.compareTo(peekMin()) <= 0) {
            if (minSize == minStack.length) minStack = java.util.Arrays.copyOf(minStack, minStack.length * 2);
            minStack[minSize++] = value;
        }
        if (maxSize == 0 || value.compareTo(peekMax()) >= 0) {
            if (maxSize == maxStack.length) maxStack = java.util.Arrays.copyOf(maxStack, maxStack.length * 2);
            maxStack[maxSize++] = value;
        }
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T value = (T) data[--size];
        data[size] = null;
        if (value.compareTo(peekMin()) == 0) minStack[--minSize] = null;
        if (value.compareTo(peekMax()) == 0) maxStack[--maxSize] = null;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return (T) data[size - 1];
    }

    @SuppressWarnings("unchecked")
    public T getMin() {
        if (isEmpty()) throw new EmptyStackException();
        return (T) minStack[minSize - 1];
    }

    @SuppressWarnings("unchecked")
    public T getMax() {
        if (isEmpty()) throw new EmptyStackException();
        return (T) maxStack[maxSize - 1];
    }

    @SuppressWarnings("unchecked")
    private T peekMin() { return (T) minStack[minSize - 1]; }

    @SuppressWarnings("unchecked")
    private T peekMax() { return (T) maxStack[maxSize - 1]; }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }

    public void clear() {
        java.util.Arrays.fill(data, 0, size, null);
        java.util.Arrays.fill(minStack, 0, minSize, null);
        java.util.Arrays.fill(maxStack, 0, maxSize, null);
        size = 0;
        minSize = 0;
        maxSize = 0;
    }

    private void ensureCapacity() {
        if (size == data.length) {
            data = java.util.Arrays.copyOf(data, data.length * 2);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Stack17[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(data[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
