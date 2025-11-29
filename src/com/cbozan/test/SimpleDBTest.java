package com.cbozan.test;

import com.cbozan.dao.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SimpleDBTest {
    public static void main(String[] args) {
        System.out.println("=== SIMPLE DATABASE CONNECTION TEST ===");
        
        try {
            // Test direct connection
            System.out.println("1. Testing H2 database connection...");
            Connection conn = DB.getFreshConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ H2 database connection successful!");
            } else {
                System.out.println("❌ Database connection failed!");
                return;
            }
            
            // Test table existence
            System.out.println("2. Testing if admin table exists...");
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM admin");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Admin table exists with " + count + " records");
            }
            
            // Test employer table
            System.out.println("3. Testing if employer table exists...");
            ps = conn.prepareStatement("SELECT COUNT(*) FROM employer");
            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Employer table exists with " + count + " records");
            }
            
            // Test insert operation
            System.out.println("4. Testing data insertion into employer table...");
            PreparedStatement insertPS = conn.prepareStatement(
                "INSERT INTO employer (fname, lname, tel, description) VALUES (?, ?, ?, ?)"
            );
            insertPS.setString(1, "Test");
            insertPS.setString(2, "User");
            insertPS.setString(3, "123-456-7890,098-765-4321");
            insertPS.setString(4, "Test employer for database persistence");
            
            int insertResult = insertPS.executeUpdate();
            if (insertResult > 0) {
                System.out.println("✅ Insert successful! " + insertResult + " row affected");
            } else {
                System.out.println("❌ Insert failed!");
            }
            
            // Verify persistence
            System.out.println("5. Verifying data persistence...");
            ps = conn.prepareStatement("SELECT * FROM employer WHERE fname = 'Test' AND lname = 'User'");
            rs = ps.executeQuery();
            if (rs.next()) {
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String tel = rs.getString("tel");
                String desc = rs.getString("description");
                System.out.println("✅ Data persisted successfully:");
                System.out.println("   Name: " + fname + " " + lname);
                System.out.println("   Phone: " + tel);
                System.out.println("   Description: " + desc);
            } else {
                System.out.println("❌ Data not found - persistence failed!");
            }
            
            // Count total records
            ps = conn.prepareStatement("SELECT COUNT(*) FROM employer");
            rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt(1);
                System.out.println("✅ Total employer records: " + total);
            }
            
            conn.close();
            System.out.println("\n=== DATABASE TEST COMPLETE - SUCCESS! ===");
            
        } catch (Exception e) {
            System.out.println("❌ Database test failed:");
            e.printStackTrace();
        }
    }
}