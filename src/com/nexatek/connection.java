
package com.nexatek;

/**
 *
 * @author engmartin
 * 
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connection {
    public static Connection connect() {
        String jdbcURL = "jdbc:postgresql://localhost:5432/luckyelectronicals";
        String username = "postgres";
        String password = "cp12345";

        try {
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            System.out.println("Connected to the PostgreSQL database!");
            return connection;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return null;
        }
    }
}
