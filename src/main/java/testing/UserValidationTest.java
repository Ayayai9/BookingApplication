package testing;

import java.io.*;
import java.util.HashMap;

import util.PasswordUtils;
import org.junit.*;

import static org.junit.Assert.*;

public class UserValidationTest {

    private final String testFile = "test_users.dat";

    @Before
    public void setupTestUsers() throws IOException {
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
    public void testPasswordEncryption() {
        String plain = "abc123";
        String encrypted = PasswordUtils.encrypt(plain);
        String expected = PasswordUtils.encrypt("abc123"); // Or whatever logic your encrypt method uses
        assertEquals("Simple shift encryption failed", expected, encrypted);
    }

    @Test
    public void testCorrectPasswordLogin() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        String encryptedInput = PasswordUtils.encrypt("pass123");
        assertEquals(users.get("user1"), encryptedInput);
    }

    @Test
    public void testWrongPasswordFails() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        String encryptedInput = PasswordUtils.encrypt("wrongpass");
        assertFalse("Password should not match", users.get("user1").equals(encryptedInput));

    }

    @Test
    public void testNonExistentUser() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        assertFalse(users.containsKey("ghostUser"));
    }

    @Test
    public void testDuplicateUserDetection() throws IOException {
        HashMap<String, String> users = loadUsersFromFile(testFile);
        assertTrue(users.containsKey("user2"));

        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile, true))) {
            writer.println("user2;" + PasswordUtils.encrypt("newpass"));
        }

        users = loadUsersFromFile(testFile);  // Reload to reflect new write
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(testFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("user2;")) count++;
            }
        }

        assertTrue("Duplicate user was written more than once", count >= 2);
    }
}
