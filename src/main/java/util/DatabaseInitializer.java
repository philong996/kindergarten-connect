package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement()) {

            // Read the schema.sql file
            String schemaPath = "schema.sql";
            String schemaSQL = new String(Files.readAllBytes(Paths.get(DatabaseInitializer.class.getClassLoader().getResource(schemaPath).toURI())));

            // Execute the schema SQL
            statement.execute(schemaSQL);
            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading schema file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}
