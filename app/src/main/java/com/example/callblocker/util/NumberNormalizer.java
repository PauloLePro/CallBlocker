package com.example.callblocker.util;

public final class NumberNormalizer {

    private NumberNormalizer() {
    }

    /**
     * Normalize French numbers to a comparable format.
     * Examples:
     * - 0162123456 -> 0162123456
     * - +33162123456 -> 0162123456
     * - 0033162123456 -> 0162123456
     */
    public static String normalizeForComparison(String raw) {
        if (raw == null) {
            return "";
        }

        String value = raw.trim();
        if (value.isEmpty()) {
            return "";
        }

        // Keep only digits and plus sign.
        value = value.replaceAll("[^0-9+]", "");

        if (value.startsWith("00")) {
            value = "+" + value.substring(2);
        }

        if (value.startsWith("+33")) {
            String rest = value.substring(3).replaceAll("\\D", "");
            if (rest.startsWith("0") && rest.length() > 1) {
                rest = rest.substring(1);
            }
            return "0" + rest;
        }

        String digitsOnly = value.replaceAll("\\D", "");
        if (digitsOnly.isEmpty()) {
            return "";
        }

        if (digitsOnly.startsWith("0033")) {
            String rest = digitsOnly.substring(4);
            if (rest.startsWith("0") && rest.length() > 1) {
                rest = rest.substring(1);
            }
            return "0" + rest;
        }

        if (digitsOnly.startsWith("33") && digitsOnly.length() >= 11) {
            String rest = digitsOnly.substring(2);
            if (rest.startsWith("0") && rest.length() > 1) {
                rest = rest.substring(1);
            }
            return "0" + rest;
        }

        // Sometimes users enter French numbers without leading 0.
        if (digitsOnly.length() == 9) {
            return "0" + digitsOnly;
        }

        return digitsOnly;
    }

    /**
     * Normalize a prefix for storage and startsWith checks.
     */
    public static String normalizePrefix(String prefix) {
        String normalized = normalizeForComparison(prefix);
        if (normalized.isEmpty()) {
            return "";
        }
        return normalized.replaceAll("[^0-9]", "");
    }
}
