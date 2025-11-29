import com.cbozan.dao.DB;
import com.cbozan.dao.EmployerDAO;
import java.sql.*;

public class ApplicationDBTest {
    public static void main(String[] args) {
        System.out.println("=== APPLICATION DATABASE CONNECTION TEST ===");
        
        try {
            // Test DB class connection
            System.out.println("1. Testing DB.getConnection()...");
            Connection conn1 = DB.getConnection();
            if (conn1 != null && !conn1.isClosed()) {
                System.out.println("✅ DB.getConnection() working");
                
                PreparedStatement ps1 = conn1.prepareStatement("SELECT COUNT(*) FROM employer");
                ResultSet rs1 = ps1.executeQuery();
                if (rs1.next()) {
                    System.out.println("✅ Found " + rs1.getInt(1) + " employers via DB.getConnection()");
                }
            } else {
                System.out.println("❌ DB.getConnection() failed");
            }
            
            // Test DB class fresh connection
            System.out.println("\n2. Testing DB.getFreshConnection()...");
            try {
                Connection conn2 = DB.getFreshConnection();
                if (conn2 != null && !conn2.isClosed()) {
                    System.out.println("✅ DB.getFreshConnection() working");
                    
                    PreparedStatement ps2 = conn2.prepareStatement("SELECT COUNT(*) FROM employer");
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next()) {
                        System.out.println("✅ Found " + rs2.getInt(1) + " employers via DB.getFreshConnection()");
                    }
                    conn2.close();
                } else {
                    System.out.println("❌ DB.getFreshConnection() failed");
                }
            } catch (Exception e) {
                System.out.println("❌ DB.getFreshConnection() exception: " + e.getMessage());
            }
            
            // Test EmployerDAO directly
            System.out.println("\n3. Testing EmployerDAO.getInstance()...");
            try {
                EmployerDAO dao = EmployerDAO.getInstance();
                if (dao != null) {
                    System.out.println("✅ EmployerDAO instance created");
                    
                    // Test list method
                    System.out.println("Testing DAO.list()...");
                    var employers = dao.list();
                    System.out.println("✅ DAO.list() returned " + employers.size() + " employers");
                    
                    // Show some details
                    for (int i = 0; i < Math.min(3, employers.size()); i++) {
                        var emp = employers.get(i);
                        System.out.println("  " + (i+1) + ". " + emp.getFname() + " " + emp.getLname());
                    }
                    
                } else {
                    System.out.println("❌ EmployerDAO.getInstance() returned null");
                }
            } catch (Exception e) {
                System.out.println("❌ EmployerDAO test failed: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("❌ Application DB test failed:");
            e.printStackTrace();
        }
    }
}