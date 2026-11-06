package io.council;

public class CreditCardValidator12 {

    public enum CardBrand { VISA, MASTERCARD, AMEX, DISCOVER, UNKNOWN }

    private CreditCardValidator12() {}

    public static boolean isValidLuhn(String number) {
        if (number == null) return false;
        String digits = number.replaceAll("\\s|-", "");
        if (!digits.matches("\\d+")) return false;
        if (digits.length() < 13 || digits.length() > 19) return false;

        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    public static CardBrand detectBrand(String number) {
        if (number == null) return CardBrand.UNKNOWN;
        String digits = number.replaceAll("\\s|-", "");
        if (!digits.matches("\\d+")) return CardBrand.UNKNOWN;

        if (digits.startsWith("4") && (digits.length() == 13 || digits.length() == 16)) {
            return CardBrand.VISA;
        }
        if (digits.length() == 16) {
            int prefix = Integer.parseInt(digits.substring(0, 4));
            if (prefix >= 2221 && prefix <= 2720) return CardBrand.MASTERCARD;
            if (prefix >= 5100 && prefix <= 5599) return CardBrand.MASTERCARD;
            if (digits.startsWith("6011") || digits.startsWith("65")) return CardBrand.DISCOVER;
        }
        if (digits.length() == 15) {
            String pre2 = digits.substring(0, 2);
            if (pre2.equals("34") || pre2.equals("37")) return CardBrand.AMEX;
        }
        return CardBrand.UNKNOWN;
    }

    public static boolean isValid(String number) {
        return isValidLuhn(number) && detectBrand(number) != CardBrand.UNKNOWN;
    }

    public static String mask(String number) {
        if (number == null) throw new IllegalArgumentException("Card number cannot be null");
        String digits = number.replaceAll("\\s|-", "");
        if (digits.length() < 4) throw new IllegalArgumentException("Card number too short");
        String masked = "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
        return masked;
    }

    public static boolean isExpired(int expiryMonth, int expiryYear) {
        if (expiryMonth < 1 || expiryMonth > 12)
            throw new IllegalArgumentException("Month must be 1-12");
        java.time.YearMonth expiry = java.time.YearMonth.of(expiryYear, expiryMonth);
        return expiry.isBefore(java.time.YearMonth.now());
    }

    public static boolean isValidCvv(String cvv, CardBrand brand) {
        if (cvv == null || !cvv.matches("\\d+")) return false;
        if (brand == CardBrand.AMEX) return cvv.length() == 4;
        return cvv.length() == 3;
    }
}
