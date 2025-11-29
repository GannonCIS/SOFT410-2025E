import java.sql.*;
import java.util.*;

public class DAOInitTest {
    public static void main(String[] args) {
        System.out.println("=== DAO INITIALIZATION TEST ===");
        
        try {
            // First, verify database has data
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
            Connection conn = DriverManager.getConnection(url, "sa", "");
            
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM employer");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Database has " + count + " employers");
            }
            
            // Test if there are any connection issues during DAO access
            System.out.println("\n=== TESTING CONNECTION DURING DAO ACCESS ===");
            
            // Simulate what happens during DAO.list() call
            try (Connection freshConn = DriverManager.getConnection(url, "sa", "")) {
                freshConn.setAutoCommit(true);
                
                Statement st = freshConn.createStatement();
                ResultSet employerRs = st.executeQuery("SELECT * FROM employer");
                
                System.out.println("✅ DAO-style connection successful");
                System.out.println("Employers found via DAO-style query:");
                
                int count = 0;
                while (employerRs.next()) {
                    count++;
                    System.out.println(count + ". ID: " + employerRs.getInt("id") + 
                                     ", Name: " + employerRs.getString("fname") + " " + employerRs.getString("lname"));
                }
                
                if (count == 0) {
                    System.out.println("❌ DAO-style query returned no results!");
                } else {
                    System.out.println("✅ DAO-style query returned " + count + " results");
                }
                
            } catch (SQLException e) {
                System.out.println("❌ DAO-style connection failed: " + e.getMessage());
            }
            
            // Test database file locking issues
            System.out.println("\n=== TESTING MULTIPLE CONNECTIONS ===");
            try (Connection conn1 = DriverManager.getConnection(url, "sa", "");
                 Connection conn2 = DriverManager.getConnection(url, "sa", "")) {
                
                System.out.println("✅ Multiple connections successful");
                
                // Test concurrent read
                PreparedStatement ps1 = conn1.prepareStatement("SELECT COUNT(*) FROM employer");
                PreparedStatement ps2 = conn2.prepareStatement("SELECT COUNT(*) FROM admin");
                
                ResultSet rs1 = ps1.executeQuery();
                ResultSet rs2 = ps2.executeQuery();
                
                if (rs1.next() && rs2.next()) {
                    System.out.println("✅ Concurrent reads: " + rs1.getInt(1) + " employers, " + rs2.getInt(1) + " admins");
                }
                
            } catch (SQLException e) {
                System.out.println("❌ Multiple connections failed: " + e.getMessage());
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.out.println("❌ Test failed:");
            e.printStackTrace();
        }
    }
}