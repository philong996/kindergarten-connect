package util;

import java.sql.*;

/**
 * Utility class for handling database image operations
 */
public class DatabaseImageUtil {
    
    /**
     * Safely sets a byte array parameter for PostgreSQL BYTEA columns
     */
    public static void setBytesParameter(PreparedStatement stmt, int parameterIndex, byte[] imageData) throws SQLException {
        if (imageData != null && imageData.length > 0) {
            // Use setBytes for PostgreSQL BYTEA columns
            stmt.setBytes(parameterIndex, imageData);
        } else {
            // Set NULL for empty or null image data
            stmt.setNull(parameterIndex, Types.BINARY);
        }
    }
    
    /**
     * Safely gets byte array from ResultSet for PostgreSQL BYTEA columns
     */
    public static byte[] getBytesFromResultSet(ResultSet rs, String columnName) throws SQLException {
        try {
            return rs.getBytes(columnName);
        } catch (SQLException e) {
            // If column doesn't exist, return null
            if (e.getMessage().contains("column") && e.getMessage().contains("does not exist")) {
                return null;
            }
            throw e;
        }
    }
    
    /**
     * Check if a column exists in the ResultSet metadata
     */
    public static boolean columnExists(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            if (metaData.getColumnName(i).equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }
}
