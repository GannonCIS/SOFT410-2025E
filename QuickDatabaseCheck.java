import java.sql.*;

public class QuickDatabaseCheck {
    public static void main(String[] args) {
        System.out.println("=== Quick Database Check ===");
        
        String url = "jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✓ Connected to H2 database");
            
            // Check employer table
            String query = "SELECT * FROM employer ORDER BY id DESC LIMIT 10";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.println("\n--- Recent Employers in Database ---");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.printf("%d. ID: %d, Name: %s %s, Phone: %s, Date: %s%n",
                        count,
                        rs.getInt("id"),
                        rs.getString("fname"),
                        rs.getString("lname"),
                        rs.getString("tel"),
                        rs.getTimestamp("date"));
                }
                
                if (count == 0) {
                    System.out.println("No employers found in database!");
                } else {
                    System.out.println("\nTotal recent employers shown: " + count);
                }
            }
            
            // Get total count
            String countQuery = "SELECT COUNT(*) as total FROM employer";
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total employers in database: " + rs.getInt("total"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}