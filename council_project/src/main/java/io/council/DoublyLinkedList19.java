package io.council;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public class DoublyLinkedList19<T extends Comparable<T>> {

    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T data) { this.data = data; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public DoublyLinkedList19() {
        head = null;
        tail = null;
        size = 0;
    }

    public void addFirst(T value) {
        if (value == null) throw new IllegalArgumentException("Null values not supported");
        Node<T> node = new Node<>(value);
        if (isEmpty()) {
            head = tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    public void addLast(T value) {
        if (value == null) throw new IllegalArgumentException("Null values not supported");
        Node<T> node = new Node<>(value);
        if (isEmpty()) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
    }

    public void add(int index, T value) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        if (index == 0) { addFirst(value); return; }
        if (index == size) { addLast(value); return; }
        Node<T> current = nodeAt(index);
        Node<T> node = new Node<>(value);
        node.next = current;
        node.prev = current.prev;
        current.prev.next = node;
        current.prev = node;
        size++;
    }

    public T removeFirst() {
        if (isEmpty()) throw new NoSuchElementException("List is empty");
        T data = head.data;
        if (size == 1) { head = tail = null; }
        else { head = head.next; head.prev = null; }
        size--;
        return data;
    }

    public T removeLast() {
        if (isEmpty()) throw new NoSuchElementException("List is empty");
        T data = tail.data;
        if (size == 1) { head = tail = null; }
        else { tail = tail.prev; tail.next = null; }
        size--;
        return data;
    }

    public T remove(int index) {
        checkIndex(index);
        if (index == 0) return removeFirst();
        if (index == size - 1) return removeLast();
        Node<T> node = nodeAt(index);
        node.prev.next = node.next;
        node.next.prev = node.prev;
        size--;
        return node.data;
    }

    public boolean remove(T value) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(value)) {
                if (current == head) { removeFirst(); return true; }
                if (current == tail) { removeLast(); return true; }
                current.prev.next = current.next;
                current.next.prev = current.prev;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public T get(int index) {
        checkIndex(index);
        return nodeAt(index).data;
    }

    public int indexOf(T value) {
        Node<T> current = head;
        int idx = 0;
        while (current != null) {
            if (current.data.equals(value)) return idx;
            current = current.next;
            idx++;
        }
        return -1;
    }

    public boolean contains(T value) { return indexOf(value) >= 0; }

    public void reverse() {
        Node<T> current = head;
        Node<T> temp = null;
        while (current != null) {
            temp = current.prev;
            current.prev = current.next;
            current.next = temp;
            current = current.prev;
        }
        if (temp != null) {
            tail = head;
            head = temp.prev;
        }
    }

    public void sort() {
        if (size <= 1) return;
        List<T> items = toList();
        items.sort(Comparator.naturalOrder());
        Node<T> current = head;
        for (T item : items) {
            current.data = item;
            current = current.next;
        }
    }

    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        Node<T> current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    private Node<T> nodeAt(int index) {
        Node<T> current;
        if (index < size / 2) {
            current = head;
            for (int i = 0; i < index; i++) current = current.next;
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) current = current.prev;
        }
        return current;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
    }

    @Override
    public String toString() {
        return "DoublyLinkedList19" + toList().toString();
    }
}
