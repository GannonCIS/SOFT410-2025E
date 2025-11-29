import java.sql.*;
import java.util.*;

public class ComprehensiveDBTest {
    public static void main(String[] args) {
        System.out.println("=== COMPREHENSIVE DATABASE TEST ===");
        
        try {
            // Direct H2 connection
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
            Connection conn = DriverManager.getConnection(url, "sa", "");
            conn.setAutoCommit(true);
            
            System.out.println("✅ Connected to H2 database!");
            
            // Check all tables
            String[] tables = {"admin", "employer", "worker", "price", "worktype", "paytype", "job", "workgroup", "work", "payment", "invoice"};
            
            for (String table : tables) {
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("✅ Table " + table + " has " + count + " records");
                    }
                } catch (SQLException e) {
                    System.out.println("❌ Table " + table + " error: " + e.getMessage());
                }
            }
            
            // Test concurrent access simulation
            System.out.println("\n=== TESTING CONCURRENT ACCESS ===");
            
            // Insert with auto-commit true
            System.out.println("Testing insert with auto-commit=true...");
            PreparedStatement ps1 = conn.prepareStatement("INSERT INTO employer (fname, lname, tel, description) VALUES (?, ?, ?, ?)");
            ps1.setString(1, "Test");
            ps1.setString(2, "AutoCommit");
            ps1.setString(3, "555-1234");
            ps1.setString(4, "Auto-commit test");
            int result1 = ps1.executeUpdate();
            System.out.println("Insert result: " + result1);
            
            // Immediately verify
            PreparedStatement ps2 = conn.prepareStatement("SELECT COUNT(*) FROM employer WHERE fname='Test' AND lname='AutoCommit'");
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                System.out.println("✅ Record immediately visible: " + rs2.getInt(1) + " records found");
            }
            
            // Test with fresh connection (simulating DAO behavior)
            System.out.println("\n=== TESTING FRESH CONNECTION ===");
            Connection freshConn = DriverManager.getConnection(url, "sa", "");
            freshConn.setAutoCommit(true);
            
            PreparedStatement ps3 = freshConn.prepareStatement("SELECT COUNT(*) FROM employer WHERE fname='Test' AND lname='AutoCommit'");
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                System.out.println("✅ Record visible from fresh connection: " + rs3.getInt(1) + " records found");
            }
            
            // Show all employers
            System.out.println("\n=== ALL EMPLOYERS IN DATABASE ===");
            PreparedStatement ps4 = freshConn.prepareStatement("SELECT id, fname, lname, tel, description FROM employer ORDER BY id");
            ResultSet rs4 = ps4.executeQuery();
            int count = 0;
            while (rs4.next()) {
                count++;
                System.out.println(count + ". ID: " + rs4.getInt("id") + 
                                 ", Name: " + rs4.getString("fname") + " " + rs4.getString("lname") +
                                 ", Tel: " + rs4.getString("tel") + 
                                 ", Desc: " + rs4.getString("description"));
            }
            System.out.println("Total employers: " + count);
            
            conn.close();
            freshConn.close();
            System.out.println("\n=== TEST COMPLETE ===");
            
        } catch (Exception e) {
            System.out.println("❌ Test failed:");
            e.printStackTrace();
        }
    }
}