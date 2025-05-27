package org.example.assignment2;



import java.io.*;
import java.util.HashMap;


class UserValidationTest {

    private final String testFile = "test_users.dat";

    @Before
    void setupTestUsers() throws IOException {
        try (PrintWriter writer = new PrintWriter(testFile)) {
            writer.println("user1;" + PasswordUtils.encrypt("pass123"));
            writer.println("user2;" + PasswordUtils.encrypt("hello456"));
        }
    }

    HashMap<String, String> loadUsersFromFile(String file) throws IOException {
        HashMap<String, String> users = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        }
        return users;
    }

    @Test
    void testPasswordEncryption() {
        String encrypted = PasswordUtils.encrypt("abc123");
        assertEquals("def456", encrypted, "Simple shift encryption failed");
    }

    @Test
    void testCorrectPasswordLogin() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        String encryptedInput = PasswordUtils.encrypt("pass123");
        assertEquals(users.get("user1"), encryptedInput);
    }

    @Test
    void testWrongPasswordFails() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        String encryptedInput = PasswordUtils.encrypt("wrongpass");
        assertNotEquals(users.get("user1"), encryptedInput);
    }

    @Test
    void testNonExistentUser() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        assertFalse(users.containsKey("ghostUser"));
    }

    @Test
    void testDuplicateUserDetection() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        assertTrue(users.containsKey("user2"));
        assertThrows(IOException.class, () -> {
            // simulate duplicate write
            try (PrintWriter writer = new PrintWriter(new FileWriter(testFile, true))) {
                writer.println("user2;" + PasswordUtils.encrypt("newpass"));
            }
        });
    }
}
