package com.freelancer.freelanceros.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class PasswordValidator {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 128;

    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]");

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password123", "password123!", "admin123", "admin1234",
            "12345678", "123456789", "qwerty1234", "letmein123", "welcome123"
    );

    private PasswordValidator() {}

    /**
     * Validates that password meets all security complexity requirements.
     * Throws ValidationException with all failed requirements.
     */
    public static void validate(String password, String username, String email) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required.");
        }

        if (password.startsWith(" ") || password.endsWith(" ")) {
            throw new ValidationException("Password must not start or end with a space.");
        }

        List<String> violations = new ArrayList<>();

        if (password.length() < MIN_LENGTH) {
            violations.add("at least " + MIN_LENGTH + " characters long");
        }
        if (password.length() > MAX_LENGTH) {
            violations.add("at most " + MAX_LENGTH + " characters long");
        }
        if (!HAS_UPPER.matcher(password).find()) {
            violations.add("at least one uppercase letter (A-Z)");
        }
        if (!HAS_LOWER.matcher(password).find()) {
            violations.add("at least one lowercase letter (a-z)");
        }
        if (!HAS_DIGIT.matcher(password).find()) {
            violations.add("at least one number (0-9)");
        }
        if (!HAS_SPECIAL.matcher(password).find()) {
            violations.add("at least one special symbol (e.g. !@#$%^&*)");
        }

        if (!violations.isEmpty()) {
            throw new ValidationException("Password must contain " + String.join(", ", violations) + ".");
        }

        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            throw new ValidationException("Password is too common and easily guessable. Please choose a stronger password.");
        }

        if (username != null && username.length() >= 3) {
            if (password.toLowerCase().contains(username.trim().toLowerCase())) {
                throw new ValidationException("Password cannot contain your username.");
            }
        }

        if (email != null && email.contains("@")) {
            String emailPrefix = email.substring(0, email.indexOf('@')).trim().toLowerCase();
            if (emailPrefix.length() >= 3 && password.toLowerCase().contains(emailPrefix)) {
                throw new ValidationException("Password cannot contain your email username.");
            }
        }
    }

    public static void validate(String password) {
        validate(password, null, null);
    }
}
