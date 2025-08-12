package util;

import java.sql.Connection;
import java.sql.Statement;

public class TestDBConnection {
    public static void main(String[] args) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            if (connection != null) {
                System.out.println("Connection to the database was successful!");

                // Test a simple query
                Statement statement = connection.createStatement();
                statement.execute("SELECT 1");
                System.out.println("Test query executed successfully.");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}