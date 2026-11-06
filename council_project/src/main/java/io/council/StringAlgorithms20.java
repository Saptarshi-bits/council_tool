package io.council;

import java.util.HashMap;
import java.util.Map;

public class StringAlgorithms20 {

    private StringAlgorithms20() {}

    public static boolean isPalindrome(String s) {
        if (s == null) throw new IllegalArgumentException("Input cannot be null");
        String cleaned = s.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        int left = 0, right = cleaned.length() - 1;
        while (left < right) {
            if (cleaned.charAt(left) != cleaned.charAt(right)) return false;
            left++;
            right--;
        }
        return true;
    }

    public static boolean isAnagram(String a, String b) {
        if (a == null || b == null) throw new IllegalArgumentException("Inputs cannot be null");
        if (a.length() != b.length()) return false;
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : a.toCharArray()) freq.merge(c, 1, Integer::sum);
        for (char c : b.toCharArray()) {
            int count = freq.getOrDefault(c, 0);
            if (count == 0) return false;
            freq.put(c, count - 1);
        }
        return true;
    }

    public static int longestSubstringWithoutRepeating(String s) {
        if (s == null) throw new IllegalArgumentException("Input cannot be null");
        Map<Character, Integer> lastSeen = new HashMap<>();
        int maxLen = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= start) {
                start = lastSeen.get(c) + 1;
            }
            lastSeen.put(c, i);
            maxLen = Math.max(maxLen, i - start + 1);
        }
        return maxLen;
    }

    public static int[] kmpSearch(String text, String pattern) {
        if (text == null || pattern == null) throw new IllegalArgumentException("Inputs cannot be null");
        if (pattern.isEmpty()) return new int[0];

        int[] lps = buildLPS(pattern);
        java.util.List<Integer> matches = new java.util.ArrayList<>();
        int i = 0, j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    matches.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return matches.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int[] buildLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    public static String reverseWords(String sentence) {
        if (sentence == null) throw new IllegalArgumentException("Input cannot be null");
        String[] words = sentence.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = words.length - 1; i >= 0; i--) {
            sb.append(words[i]);
            if (i > 0) sb.append(" ");
        }
        return sb.toString();
    }

    public static int countOccurrences(String text, String pattern) {
        if (text == null || pattern == null) throw new IllegalArgumentException("Inputs cannot be null");
        if (pattern.isEmpty()) return 0;
        return kmpSearch(text, pattern).length;
    }

    public static boolean isRotation(String a, String b) {
        if (a == null || b == null) throw new IllegalArgumentException("Inputs cannot be null");
        if (a.length() != b.length()) return false;
        if (a.isEmpty()) return true;
        return (a + a).contains(b);
    }
}
