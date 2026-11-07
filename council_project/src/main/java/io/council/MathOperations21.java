package io.council;

import java.math.BigInteger;

public class MathOperations21 {

    private MathOperations21() {}

    public static long gcd(long a, long b) {
        if (a < 0 || b < 0) throw new IllegalArgumentException("Arguments must be non-negative");
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static long lcm(long a, long b) {
        if (a < 0 || b < 0) throw new IllegalArgumentException("Arguments must be non-negative");
        if (a == 0 || b == 0) return 0;
        return a / gcd(a, b) * b;
    }

    public static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    public static BigInteger factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Factorial is undefined for negative numbers");
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    public static long fibonacci(int n) {
        if (n < 0) throw new IllegalArgumentException("Fibonacci is undefined for negative indices");
        if (n == 0) return 0;
        if (n == 1) return 1;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long c = a + b;
            a = b;
            b = c;
        }
        return b;
    }

    public static long power(long base, int exp) {
        if (exp < 0) throw new IllegalArgumentException("Exponent must be non-negative");
        long result = 1;
        long b = base;
        int e = exp;
        while (e > 0) {
            if ((e & 1) == 1) result *= b;
            b *= b;
            e >>= 1;
        }
        return result;
    }

    public static long power(long base, int exp, long mod) {
        if (mod < 1) throw new IllegalArgumentException("Modulus must be positive");
        if (exp < 0) throw new IllegalArgumentException("Exponent must be non-negative");
        long result = 1;
        long b = base % mod;
        int e = exp;
        while (e > 0) {
            if ((e & 1) == 1) result = result * b % mod;
            b = b * b % mod;
            e >>= 1;
        }
        return result;
    }

    public static boolean isPerfectSquare(long n) {
        if (n < 0) return false;
        long root = (long) Math.sqrt(n);
        return root * root == n;
    }

    public static int[] primeFactors(int n) {
        if (n < 2) throw new IllegalArgumentException("Input must be at least 2");
        java.util.List<Integer> factors = new java.util.ArrayList<>();
        for (int d = 2; (long) d * d <= n; d++) {
            while (n % d == 0) {
                factors.add(d);
                n /= d;
            }
        }
        if (n > 1) factors.add(n);
        return factors.stream().mapToInt(Integer::intValue).toArray();
    }

    public static long combinations(int n, int k) {
        if (n < 0 || k < 0) throw new IllegalArgumentException("n and k must be non-negative");
        if (k > n) return 0;
        if (k == 0 || k == n) return 1;
        k = Math.min(k, n - k);
        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }
}
