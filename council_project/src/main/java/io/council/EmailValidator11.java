package io.council;

public class EmailValidator11 {

    private static final int MAX_LOCAL_LENGTH = 64;
    private static final int MAX_DOMAIN_LENGTH = 255;
    private static final int MAX_TOTAL_LENGTH = 320;

    private EmailValidator11() {}

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) return false;
        if (email.length() > MAX_TOTAL_LENGTH) return false;

        int atIndex = email.lastIndexOf('@');
        if (atIndex < 1) return false;
        if (atIndex == email.length() - 1) return false;

        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);

        return isValidLocal(local) && isValidDomain(domain);
    }

    private static boolean isValidLocal(String local) {
        if (local.isEmpty() || local.length() > MAX_LOCAL_LENGTH) return false;
        if (local.startsWith(".") || local.endsWith(".")) return false;
        if (local.contains("..")) return false;

        for (char c : local.toCharArray()) {
            if (!isAllowedLocalChar(c)) return false;
        }
        return true;
    }

    private static boolean isValidDomain(String domain) {
        if (domain.isEmpty() || domain.length() > MAX_DOMAIN_LENGTH) return false;
        if (domain.startsWith(".") || domain.endsWith(".")) return false;
        if (domain.startsWith("-")) return false;

        String[] labels = domain.split("\\.", -1);
        if (labels.length < 2) return false;

        String tld = labels[labels.length - 1];
        if (tld.length() < 2) return false;

        for (String label : labels) {
            if (label.isEmpty()) return false;
            if (label.length() > 63) return false;
            if (label.startsWith("-") || label.endsWith("-")) return false;
            for (char c : label.toCharArray()) {
                if (!Character.isLetterOrDigit(c) && c != '-') return false;
            }
        }
        return true;
    }

    private static boolean isAllowedLocalChar(char c) {
        return Character.isLetterOrDigit(c)
                || c == '.' || c == '_' || c == '+' || c == '-'
                || c == '!' || c == '#' || c == '$' || c == '%'
                || c == '&' || c == '\'' || c == '*' || c == '/'
                || c == '=' || c == '?' || c == '^' || c == '`'
                || c == '{' || c == '|' || c == '}' || c == '~';
    }

    public static String extractDomain(String email) {
        if (!isValid(email)) throw new IllegalArgumentException("Invalid email: " + email);
        return email.substring(email.lastIndexOf('@') + 1).toLowerCase();
    }

    public static String normalizeEmail(String email) {
        if (!isValid(email)) throw new IllegalArgumentException("Invalid email: " + email);
        int atIndex = email.lastIndexOf('@');
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1).toLowerCase();
        int plusIndex = local.indexOf('+');
        if (plusIndex >= 0) local = local.substring(0, plusIndex);
        return local.toLowerCase() + "@" + domain;
    }

    public static boolean isSameDomain(String email1, String email2) {
        if (!isValid(email1) || !isValid(email2)) return false;
        return extractDomain(email1).equalsIgnoreCase(extractDomain(email2));
    }
}
