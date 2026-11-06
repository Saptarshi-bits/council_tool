package io.council;

import java.util.Arrays;

public class SortingAlgorithms05 {

    public static int[] bubbleSort(int[] arr) {
        if (arr == null) throw new IllegalArgumentException("Array cannot be null");
        int[] a = Arrays.copyOf(arr, arr.length);
        for (int i = 0; i < a.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int tmp = a[j]; a[j] = a[j + 1]; a[j + 1] = tmp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
        return a;
    }

    public static int[] mergeSort(int[] arr) {
        if (arr == null) throw new IllegalArgumentException("Array cannot be null");
        int[] a = Arrays.copyOf(arr, arr.length);
        if (a.length <= 1) return a;
        mergeSortRec(a, 0, a.length - 1);
        return a;
    }

    private static void mergeSortRec(int[] a, int left, int right) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSortRec(a, left, mid);
        mergeSortRec(a, mid + 1, right);
        merge(a, left, mid, right);
    }

    private static void merge(int[] a, int left, int mid, int right) {
        int[] tmp = Arrays.copyOfRange(a, left, right + 1);
        int i = 0, j = mid - left + 1, k = left;
        while (i <= mid - left && j <= right - left) {
            if (tmp[i] <= tmp[j]) a[k++] = tmp[i++];
            else a[k++] = tmp[j++];
        }
        while (i <= mid - left) a[k++] = tmp[i++];
        while (j <= right - left) a[k++] = tmp[j++];
    }

    public static int[] quickSort(int[] arr) {
        if (arr == null) throw new IllegalArgumentException("Array cannot be null");
        int[] a = Arrays.copyOf(arr, arr.length);
        quickSortRec(a, 0, a.length - 1);
        return a;
    }

    private static void quickSortRec(int[] a, int low, int high) {
        if (low >= high) return;
        int p = partition(a, low, high);
        quickSortRec(a, low, p - 1);
        quickSortRec(a, p + 1, high);
    }

    private static int partition(int[] a, int low, int high) {
        int pivot = a[high], i = low - 1;
        for (int j = low; j < high; j++) {
            if (a[j] <= pivot) {
                i++;
                int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
            }
        }
        int tmp = a[i + 1]; a[i + 1] = a[high]; a[high] = tmp;
        return i + 1;
    }

    public static int binarySearch(int[] arr, int target) {
        if (arr == null) throw new IllegalArgumentException("Array cannot be null");
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (arr[mid] == target) return mid;
            if (arr[mid] < target) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }

    public static int findKthLargest(int[] arr, int k) {
        if (arr == null || arr.length == 0) throw new IllegalArgumentException("Array cannot be null or empty");
        if (k < 1 || k > arr.length) throw new IllegalArgumentException("k out of range");
        int[] sorted = mergeSort(arr);
        return sorted[sorted.length - k];
    }

    public static boolean isSorted(int[] arr) {
        if (arr == null) throw new IllegalArgumentException("Array cannot be null");
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) return false;
        }
        return true;
    }

    public static int[] removeDuplicates(int[] sortedArr) {
        if (sortedArr == null) throw new IllegalArgumentException("Array cannot be null");
        if (sortedArr.length == 0) return new int[0];
        int[] tmp = new int[sortedArr.length];
        int count = 0;
        tmp[count++] = sortedArr[0];
        for (int i = 1; i < sortedArr.length; i++) {
            if (sortedArr[i] != sortedArr[i - 1]) tmp[count++] = sortedArr[i];
        }
        return Arrays.copyOf(tmp, count);
    }
}
