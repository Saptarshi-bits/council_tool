package io.council;

import java.util.NoSuchElementException;

public class CircularQueue18<T> {

    private final Object[] buffer;
    private int head;
    private int tail;
    private int size;
    private final int capacity;

    public CircularQueue18(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1");
        this.capacity = capacity;
        this.buffer = new Object[capacity];
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    public boolean enqueue(T value) {
        if (value == null) throw new IllegalArgumentException("Null values not supported");
        if (isFull()) return false;
        buffer[tail] = value;
        tail = (tail + 1) % capacity;
        size++;
        return true;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        T value = (T) buffer[head];
        buffer[head] = null;
        head = (head + 1) % capacity;
        size--;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        return (T) buffer[head];
    }

    @SuppressWarnings("unchecked")
    public T peekLast() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        int lastIndex = (tail - 1 + capacity) % capacity;
        return (T) buffer[lastIndex];
    }

    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == capacity; }
    public int size() { return size; }
    public int capacity() { return capacity; }
    public int remainingCapacity() { return capacity - size; }

    public void clear() {
        java.util.Arrays.fill(buffer, null);
        head = 0;
        tail = 0;
        size = 0;
    }

    public boolean contains(T value) {
        if (value == null) return false;
        for (int i = 0; i < size; i++) {
            if (value.equals(buffer[(head + i) % capacity])) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        Object[] arr = new Object[size];
        for (int i = 0; i < size; i++) {
            arr[i] = buffer[(head + i) % capacity];
        }
        return arr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CircularQueue18[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(buffer[(head + i) % capacity]);
        }
        sb.append("]");
        return sb.toString();
    }
}
