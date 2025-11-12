package com.cbozan.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.cbozan.dao.DB;

/**
 * Database initialization utility.
 * Creates sample data and admin users for testing the application.
 */
public class DatabaseInitializer {
    
    public static void initializeSampleData() {
        Connection conn = DB.getConnection();
        if (conn == null) {
            System.err.println("Cannot initialize sample data - no database connection");
            return;
        }
        
        try {
            // Create admin table and default admin user
            createAdminTable(conn);
            insertDefaultAdmin(conn);
            
            // Create sample data only if tables are empty
            if (DB.isUsingH2()) {
                createSampleData(conn);
            }
            
            System.out.println("Database initialization completed successfully");
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createAdminTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (DB.isUsingH2()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS admin (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            } else {
                // PostgreSQL version
                stmt.execute("CREATE TABLE IF NOT EXISTS admin (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            }
        }
    }
    
    private static void insertDefaultAdmin(Connection conn) throws SQLException {
        // Check if admin already exists
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM admin WHERE username = ?")) {
            ps.setString(1, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Default admin user already exists");
                    return;
                }
            }
        }
        
        // Insert default admin
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO admin (username, password, email) VALUES (?, ?, ?)")) {
            ps.setString(1, "admin");
            ps.setString(2, "admin");
            ps.setString(3, "admin@example.com");
            ps.executeUpdate();
            System.out.println("Created default admin user (admin/admin)");
        }
    }
    
    private static void createSampleData(Connection conn) throws SQLException {
        // Check if sample data already exists
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM employer")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Sample data already exists");
                    return;
                }
            }
        }
        
        System.out.println("Creating sample data...");
        
        // Insert sample employers
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO employer (fname, lname, tel, description) VALUES (?, ?, ?, ?)")) {
            
            ps.setString(1, "John");
            ps.setString(2, "Smith");
            ps.setString(3, "555-0101,555-0102");
            ps.setString(4, "Construction company owner");
            ps.executeUpdate();
            
            ps.setString(1, "Jane");
            ps.setString(2, "Doe");
            ps.setString(3, "555-0201");
            ps.setString(4, "Restaurant owner");
            ps.executeUpdate();
        }
        
        // Insert sample workers
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO worker (fname, lname, tel, description) VALUES (?, ?, ?, ?)")) {
            
            ps.setString(1, "Mike");
            ps.setString(2, "Johnson");
            ps.setString(3, "555-1001");
            ps.setString(4, "Experienced carpenter");
            ps.executeUpdate();
            
            ps.setString(1, "Sarah");
            ps.setString(2, "Wilson");
            ps.setString(3, "555-1002");
            ps.setString(4, "Skilled painter");
            ps.executeUpdate();
            
            ps.setString(1, "David");
            ps.setString(2, "Brown");
            ps.setString(3, "555-1003");
            ps.setString(4, "Kitchen staff");
            ps.executeUpdate();
        }
        
        // Insert sample work types
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO worktype (name, description) VALUES (?, ?)")) {
            
            ps.setString(1, "Carpentry");
            ps.setString(2, "Wood construction and finishing");
            ps.executeUpdate();
            
            ps.setString(1, "Painting");
            ps.setString(2, "Interior and exterior painting");
            ps.executeUpdate();
            
            ps.setString(1, "Kitchen");
            ps.setString(2, "Food preparation and service");
            ps.executeUpdate();
        }
        
        // Insert sample work groups
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO workgroup (name, description) VALUES (?, ?)")) {
            
            ps.setString(1, "Construction Team");
            ps.setString(2, "Building construction workers");
            ps.executeUpdate();
            
            ps.setString(1, "Restaurant Staff");
            ps.setString(2, "Restaurant kitchen and service staff");
            ps.executeUpdate();
        }
        
        // Insert sample pay types
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO paytype (name, description) VALUES (?, ?)")) {
            
            ps.setString(1, "Hourly");
            ps.setString(2, "Payment per hour worked");
            ps.executeUpdate();
            
            ps.setString(1, "Daily");
            ps.setString(2, "Fixed payment per day");
            ps.executeUpdate();
            
            ps.setString(1, "Project");
            ps.setString(2, "Payment for completed project");
            ps.executeUpdate();
        }
        
        // Insert sample jobs
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO job (employer_id, name, description) VALUES (?, ?, ?)")) {
            
            ps.setInt(1, 1); // John Smith
            ps.setString(2, "House Renovation");
            ps.setString(3, "Complete renovation of 3-bedroom house");
            ps.executeUpdate();
            
            ps.setInt(1, 2); // Jane Doe
            ps.setString(2, "Restaurant Opening");
            ps.setString(3, "Staff needed for new restaurant opening");
            ps.executeUpdate();
        }
        
        // Insert sample prices
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO price (worktype_id, paytype_id, amount, description) VALUES (?, ?, ?, ?)")) {
            
            ps.setInt(1, 1); // Carpentry
            ps.setInt(2, 1); // Hourly
            ps.setBigDecimal(3, new java.math.BigDecimal("25.00"));
            ps.setString(4, "Standard carpentry hourly rate");
            ps.executeUpdate();
            
            ps.setInt(1, 2); // Painting
            ps.setInt(2, 1); // Hourly
            ps.setBigDecimal(3, new java.math.BigDecimal("20.00"));
            ps.setString(4, "Standard painting hourly rate");
            ps.executeUpdate();
            
            ps.setInt(1, 3); // Kitchen
            ps.setInt(2, 2); // Daily
            ps.setBigDecimal(3, new java.math.BigDecimal("120.00"));
            ps.setString(4, "Kitchen staff daily rate");
            ps.executeUpdate();
        }
        
        System.out.println("Sample data created successfully");
    }
}