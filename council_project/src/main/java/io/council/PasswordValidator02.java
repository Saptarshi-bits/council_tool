package io.council;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator02 {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT     = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL   = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public enum Strength { WEAK, MODERATE, STRONG, VERY_STRONG }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final Strength strength;

        ValidationResult(boolean valid, List<String> errors, Strength strength) {
            this.valid = valid;
            this.errors = errors;
            this.strength = strength;
        }
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public Strength getStrength() { return strength; }
    }

    public ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null) {
            errors.add("Password cannot be null");
            return new ValidationResult(false, errors, Strength.WEAK);
        }
        if (password.length() < MIN_LENGTH)
            errors.add("Must be at least " + MIN_LENGTH + " characters");
        if (password.length() > MAX_LENGTH)
            errors.add("Must not exceed " + MAX_LENGTH + " characters");
        if (!UPPERCASE.matcher(password).find())
            errors.add("Must contain at least one uppercase letter");
        if (!LOWERCASE.matcher(password).find())
            errors.add("Must contain at least one lowercase letter");
        if (!DIGIT.matcher(password).find())
            errors.add("Must contain at least one digit");
        if (!SPECIAL.matcher(password).find())
            errors.add("Must contain at least one special character");

        boolean valid = errors.isEmpty();
        Strength strength = valid ? calculateStrength(password) : Strength.WEAK;
        return new ValidationResult(valid, errors, strength);
    }

    private Strength calculateStrength(String password) {
        int score = 0;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;
        if (SPECIAL.matcher(password).results().count() >= 2) score++;
        if (DIGIT.matcher(password).results().count() >= 2) score++;

        return switch (score) {
            case 0 -> Strength.MODERATE;
            case 1, 2 -> Strength.STRONG;
            default -> Strength.VERY_STRONG;
        };
    }

    public boolean isCommonPassword(String password) {
        if (password == null) return false;
        List<String> common = List.of(
            "Password1!", "Admin123!", "Welcome1!", "Summer2024!",
            "Qwerty123!", "Dragon123!", "Monkey123!"
        );
        return common.contains(password);
    }

    public static String maskPassword(String password) {
        if (password == null) return null;
        if (password.length() <= 2) return "*".repeat(password.length());
        return password.charAt(0) + "*".repeat(password.length() - 2) + password.charAt(password.length() - 1);
    }
}
