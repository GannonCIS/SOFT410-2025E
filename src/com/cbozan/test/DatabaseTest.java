package com.cbozan.test;

import com.cbozan.dao.DB;
import com.cbozan.dao.EmployerDAO;
import com.cbozan.entity.Employer;
import com.cbozan.entity.EmployerBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("=== DATABASE PERSISTENCE TEST ===");
        
        try {
            // Test 1: Connection Test
            System.out.println("1. Testing database connection...");
            Connection conn = DB.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Database connection failed!");
                return;
            }
            
            // Test 2: Table existence
            System.out.println("2. Testing table existence...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM employer");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Employer table exists with " + count + " records");
            }
            
            // Test 3: Insert Test
            System.out.println("3. Testing data insertion...");
            EmployerBuilder builder = new EmployerBuilder();
            builder.setFname("Test");
            builder.setLname("Employer");
            List<String> phones = new ArrayList<>();
            phones.add("123-456-7890");
            phones.add("098-765-4321");
            builder.setTel(phones);
            builder.setDescription("Test employer for database persistence");
            
            Employer testEmployer = builder.build();
            
            EmployerDAO dao = EmployerDAO.getInstance();
            boolean insertResult = dao.create(testEmployer);
            
            if (insertResult) {
                System.out.println("✅ Data insertion successful!");
            } else {
                System.out.println("❌ Data insertion failed!");
            }
            
            // Test 4: Data Persistence Verification
            System.out.println("4. Verifying data persistence...");
            rs = stmt.executeQuery("SELECT * FROM employer WHERE fname = 'Test' AND lname = 'Employer'");
            if (rs.next()) {
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String tel = rs.getString("tel");
                String description = rs.getString("description");
                
                System.out.println("✅ Data successfully persisted in database:");
                System.out.println("   Name: " + fname + " " + lname);
                System.out.println("   Phone: " + tel);
                System.out.println("   Description: " + description);
            } else {
                System.out.println("❌ Data not found in database - persistence failed!");
            }
            
            // Test 5: Count total records
            rs = stmt.executeQuery("SELECT COUNT(*) FROM employer");
            if (rs.next()) {
                int totalRecords = rs.getInt(1);
                System.out.println("✅ Total employer records in database: " + totalRecords);
            }
            
            System.out.println("\n=== DATABASE TEST COMPLETE ===");
            System.out.println("Database file location: ./data/hesap-eproject.mv.db");
            
        } catch (Exception e) {
            System.out.println("❌ Database test failed with error:");
            e.printStackTrace();
        }
    }
}