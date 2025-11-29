import com.cbozan.dao.EmployerDAO;
import com.cbozan.entity.Employer;
import java.util.List;
import java.util.ArrayList;

public class RealTimeSaveTest {
    public static void main(String[] args) {
        System.out.println("=== Real-Time Save Test ===");
        
        try {
            EmployerDAO dao = EmployerDAO.getInstance();
            
            // Check initial state
            List<Employer> before = dao.list();
            System.out.println("Employers before test: " + before.size());
            
            // Create a test employer using the actual DAO
            System.out.println("\nTesting DAO create method...");
            
            // Use the actual builder that the application uses
            Employer.EmployerBuilder builder = new Employer.EmployerBuilder();
            builder.setId(Integer.MAX_VALUE);
            builder.setFname("REALTIME");
            builder.setLname("TEST" + System.currentTimeMillis());
            
            List<String> phones = new ArrayList<>();
            phones.add("05551234567"); // Turkish format
            builder.setTel(phones);
            
            builder.setDescription("Real-time save test - " + new java.util.Date());
            
            Employer testEmployer = builder.build();
            System.out.println("Created test employer: " + testEmployer.getFname() + " " + testEmployer.getLname());
            
            // Try to save
            boolean saved = dao.create(testEmployer);
            System.out.println("DAO.create() result: " + saved);
            
            if (saved) {
                // Check if it was really saved
                List<Employer> after = dao.list();
                System.out.println("Employers after test: " + after.size());
                
                if (after.size() > before.size()) {
                    System.out.println("✓ SUCCESS: New employer was saved to database!");
                    
                    // Find the new employer
                    for (Employer emp : after) {
                        if (emp.getFname().equals("REALTIME")) {
                            System.out.println("  Found: ID=" + emp.getId() + 
                                             ", Name=" + emp.getFname() + " " + emp.getLname() +
                                             ", Phone=" + emp.getTel());
                            break;
                        }
                    }
                } else {
                    System.out.println("✗ ERROR: Employer count didn't increase!");
                }
            } else {
                System.out.println("✗ ERROR: DAO.create() returned false!");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}