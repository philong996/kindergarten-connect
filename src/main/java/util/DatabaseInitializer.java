package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    /**
     * Check if the database is already initialized by checking if the 'users' table exists
     * @return true if database is initialized, false otherwise
     */
    public boolean isDatabaseInitialized() {
        try (Connection connection = DatabaseUtil.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "users", null);
            boolean tableExists = tables.next();
            tables.close();
            return tableExists;
        } catch (SQLException e) {
            System.err.println("Error checking database status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Initialize the database by creating tables and inserting sample data
     */
    public void initializeDatabase() {
        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement()) {

            // Read the schema.sql file
            String schemaPath = "schema.sql";
            String schemaSQL = new String(Files.readAllBytes(Paths.get(DatabaseInitializer.class.getClassLoader().getResource(schemaPath).toURI())));

            // Execute the schema SQL
            statement.execute(schemaSQL);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        } catch (IOException e) {
            System.err.println("Error reading schema file: " + e.getMessage());
            throw new RuntimeException("Failed to read schema file", e);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error during database initialization", e);
        }
    }

    public static void main(String[] args) {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeDatabase();
    }
}
