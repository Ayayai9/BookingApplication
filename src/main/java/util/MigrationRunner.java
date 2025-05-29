package util;

import databse.UserJDBC;

public class MigrationRunner {
    public static void main(String[] args) {
        UserJDBC.initializeDatabase(); // Ensure table exists
        UserMigration.migrateUsersFromFile("users.dat");
    }
}

