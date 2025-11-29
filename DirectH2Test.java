import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DirectH2Test {
    public static void main(String[] args) {
        System.out.println("=== DIRECT H2 DATABASE VERIFICATION ===");
        
        try {
            // Direct H2 connection
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
            Connection conn = DriverManager.getConnection(url, "sa", "");
            conn.setAutoCommit(true);
            
            System.out.println("✅ Connected directly to H2 database!");
            
            // Check admin table
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM admin");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("✅ Admin table has " + rs.getInt(1) + " records");
            }
            
            // Check and show all employers
            ps = conn.prepareStatement("SELECT COUNT(*) FROM employer");
            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Employer table has " + count + " records");
                
                if (count > 0) {
                    System.out.println("Showing all employers:");
                    ps = conn.prepareStatement("SELECT id, fname, lname, tel, description FROM employer LIMIT 10");
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        System.out.println("  ID: " + rs.getInt("id") + 
                                         ", Name: " + rs.getString("fname") + " " + rs.getString("lname") +
                                         ", Tel: " + rs.getString("tel") + 
                                         ", Desc: " + rs.getString("description"));
                    }
                }
            }
            
            // Insert a test employer to verify persistence
            System.out.println("Testing insert operation...");
            ps = conn.prepareStatement("INSERT INTO employer (fname, lname, tel, description) VALUES (?, ?, ?, ?)");
            ps.setString(1, "Direct");
            ps.setString(2, "Test");
            ps.setString(3, "111-222-3333,444-555-6666");
            ps.setString(4, "Direct H2 test insertion");
            
            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("✅ Successfully inserted test record!");
                
                // Verify the insert
                ps = conn.prepareStatement("SELECT * FROM employer WHERE fname = 'Direct' AND lname = 'Test'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("✅ Test record verified in database:");
                    System.out.println("  ID: " + rs.getInt("id"));
                    System.out.println("  Name: " + rs.getString("fname") + " " + rs.getString("lname"));
                    System.out.println("  Phone: " + rs.getString("tel"));
                    System.out.println("  Description: " + rs.getString("description"));
                }
            }
            
            // Final count
            ps = conn.prepareStatement("SELECT COUNT(*) FROM employer");
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("✅ Final employer count: " + rs.getInt(1));
            }
            
            conn.close();
            System.out.println("\n=== DATABASE PERSISTENCE VERIFICATION COMPLETE ✅ ===");
            System.out.println("Database is working correctly and saving data!");
            
        } catch (Exception e) {
            System.out.println("❌ Database verification failed:");
            e.printStackTrace();
        }
    }
}