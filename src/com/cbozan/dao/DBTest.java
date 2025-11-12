package com.cbozan.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test database connection and initialization
 */
public class DBTest {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("DATABASE CONNECTION TEST");
        System.out.println("=".repeat(60));
        System.out.println();
        
        System.out.println("Configuration:");
        System.out.println("  DB Type: " + DBConfig.DB_TYPE);
        System.out.println("  JDBC URL: " + DBConfig.getJdbcUrl());
        System.out.println("  Username: " + DBConfig.getUsername());
        System.out.println();
        
        System.out.println("Attempting connection...");
        Connection conn = DB.getConnection();
        
        if (conn == null) {
            System.err.println("❌ Connection is NULL!");
            return;
        }
        
        try {
            if (conn.isClosed()) {
                System.err.println("❌ Connection is CLOSED!");
                return;
            }
            
            System.out.println("✅ Connection successful!");
            System.out.println();
            
            // Test queries
            System.out.println("Testing database...");
            Statement stmt = conn.createStatement();
            
            // Count tables
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'"
            );
            rs.next();
            int tableCount = rs.getInt(1);
            System.out.println("  Tables found: " + tableCount);
            
            // List all tables
            rs = stmt.executeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' ORDER BY TABLE_NAME"
            );
            System.out.println("  Table names:");
            while (rs.next()) {
                System.out.println("    - " + rs.getString(1));
            }
            
            // Count records in key tables
            System.out.println();
            System.out.println("Sample data:");
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM admin");
            rs.next();
            System.out.println("  Admin users: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM employer");
            rs.next();
            System.out.println("  Employers: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM worker");
            rs.next();
            System.out.println("  Workers: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM job");
            rs.next();
            System.out.println("  Jobs: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM price");
            rs.next();
            System.out.println("  Price tiers: " + rs.getInt(1));
            
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("✅ DATABASE TEST PASSED!");
            System.out.println("=".repeat(60));
            
        } catch (Exception e) {
            System.err.println("❌ Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
