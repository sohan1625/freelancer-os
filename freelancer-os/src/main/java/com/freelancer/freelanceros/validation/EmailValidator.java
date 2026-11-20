package com.freelancer.freelanceros.validation;

import java.util.regex.Pattern;

public final class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    // Standard RFC-compliant email pattern
    private static final Pattern GENERAL_EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$"
    );

    // Gmail local-part pattern: only letters, numbers, and periods (and optional +tag alias)
    private static final Pattern GMAIL_USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*(?:\\+[a-zA-Z0-9._-]+)?$"
    );

    private EmailValidator() {}

    /**
     * Validates and returns the normalized (trimmed, lowercase) email address.
     * Throws ValidationException if the email is invalid.
     */
    public static String validateAndNormalize(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email address is required.");
        }

        String trimmed = email.trim();

        if (trimmed.length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException("Email address cannot exceed " + MAX_EMAIL_LENGTH + " characters.");
        }

        int atIndex = trimmed.indexOf('@');
        if (atIndex == -1 || atIndex != trimmed.lastIndexOf('@')) {
            throw new ValidationException("Email address must contain exactly one '@' symbol.");
        }

        String localPart = trimmed.substring(0, atIndex);
        String domain = trimmed.substring(atIndex + 1).toLowerCase();

        if (localPart.isBlank() || localPart.length() > MAX_LOCAL_PART_LENGTH) {
            throw new ValidationException("Email username part must be between 1 and " + MAX_LOCAL_PART_LENGTH + " characters.");
        }

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            throw new ValidationException("Email address cannot start or end with a period ('.').");
        }

        if (localPart.contains("..")) {
            throw new ValidationException("Email address cannot contain consecutive periods ('..').");
        }

        if (!GENERAL_EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Please provide a valid email format (e.g., name@example.com).");
        }

        // Specific validation for Gmail / Google Workspace domains
        if (domain.equals("gmail.com") || domain.equals("googlemail.com")) {
            validateGmail(localPart);
        }

        return trimmed.toLowerCase();
    }

    /**
     * Strict validation for Gmail addresses according to Google account specifications:
     * - Base username length between 6 and 30 characters
     * - Only letters (a-z), numbers (0-9), and periods (.) allowed in username
     * - Cannot start or end with a period
     * - No consecutive periods
     */
    public static void validateGmail(String localPart) {
        String baseUsername = localPart;
        int plusIndex = baseUsername.indexOf('+');
        if (plusIndex != -1) {
            baseUsername = baseUsername.substring(0, plusIndex);
        }

        // Dots do not count towards Gmail unique username characters for length
        String strippedDots = baseUsername.replace(".", "");

        if (strippedDots.length() < 6) {
            throw new ValidationException("Gmail username must be at least 6 characters long.");
        }

        if (strippedDots.length() > 30) {
            throw new ValidationException("Gmail username cannot exceed 30 characters.");
        }

        if (!GMAIL_USERNAME_PATTERN.matcher(localPart).matches()) {
            throw new ValidationException(
                    "Invalid Gmail address format. Gmail only permits letters (a-z), numbers (0-9), and non-consecutive periods."
            );
        }
    }

    public static boolean isValid(String email) {
        try {
            validateAndNormalize(email);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
