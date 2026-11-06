package io.council;

import java.util.ArrayList;
import java.util.List;

public class BinarySearchTree04 {

    private static class Node {
        int value;
        Node left, right;
        Node(int value) { this.value = value; }
    }

    private Node root;
    private int size;

    public void insert(int value) {
        root = insertRec(root, value);
    }

    private Node insertRec(Node node, int value) {
        if (node == null) { size++; return new Node(value); }
        if (value < node.value) node.left = insertRec(node.left, value);
        else if (value > node.value) node.right = insertRec(node.right, value);
        return node;
    }

    public boolean contains(int value) {
        return containsRec(root, value);
    }

    private boolean containsRec(Node node, int value) {
        if (node == null) return false;
        if (value == node.value) return true;
        return value < node.value ? containsRec(node.left, value) : containsRec(node.right, value);
    }

    public void delete(int value) {
        if (!contains(value)) throw new IllegalArgumentException("Value not found: " + value);
        root = deleteRec(root, value);
        size--;
    }

    private Node deleteRec(Node node, int value) {
        if (node == null) return null;
        if (value < node.value) { node.left = deleteRec(node.left, value); }
        else if (value > node.value) { node.right = deleteRec(node.right, value); }
        else {
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            int minRight = findMin(node.right);
            node.value = minRight;
            node.right = deleteRec(node.right, minRight);
        }
        return node;
    }

    public int findMin() {
        if (root == null) throw new IllegalStateException("Tree is empty");
        return findMin(root);
    }

    private int findMin(Node node) {
        return node.left == null ? node.value : findMin(node.left);
    }

    public int findMax() {
        if (root == null) throw new IllegalStateException("Tree is empty");
        return findMax(root);
    }

    private int findMax(Node node) {
        return node.right == null ? node.value : findMax(node.right);
    }

    public int height() {
        return heightRec(root);
    }

    private int heightRec(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(heightRec(node.left), heightRec(node.right));
    }

    public List<Integer> inorder() {
        List<Integer> result = new ArrayList<>();
        inorderRec(root, result);
        return result;
    }

    private void inorderRec(Node node, List<Integer> result) {
        if (node == null) return;
        inorderRec(node.left, result);
        result.add(node.value);
        inorderRec(node.right, result);
    }

    public List<Integer> levelOrder() {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;
        java.util.Queue<Node> queue = new java.util.LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            result.add(current.value);
            if (current.left != null) queue.add(current.left);
            if (current.right != null) queue.add(current.right);
        }
        return result;
    }

    public boolean isBST() {
        return isBSTRec(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private boolean isBSTRec(Node node, int min, int max) {
        if (node == null) return true;
        if (node.value <= min || node.value >= max) return false;
        return isBSTRec(node.left, min, node.value) && isBSTRec(node.right, node.value, max);
    }

    public int getSize() { return size; }
    public boolean isEmpty() { return size == 0; }
}
