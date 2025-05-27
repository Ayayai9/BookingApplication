package org.example.assignment2;

public class PasswordUtils {
    public static String encrypt(String plain) {
        StringBuilder sb = new StringBuilder();
        for (char c : plain.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append((char) (c + 3)); // Shift by +3 (e.g., a->d, 1->4)
            } else {
                sb.append(c); // Leave special characters unchanged
            }
        }
        return sb.toString();
    }

    public static String decrypt(String encrypted) {
        StringBuilder sb = new StringBuilder();
        for (char c : encrypted.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append((char) (c - 3)); // Reverse the shift
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

