import com.cbozan.dao.EmployerDAO;
import com.cbozan.entity.Employer;
import com.cbozan.entity.EmployerBuilder;
import com.cbozan.entity.EntityException;
import java.util.List;
import java.util.ArrayList;

public class CurrentDatabaseStateTest {
    public static void main(String[] args) {
        System.out.println("=== Current Database State Test ===");
        
        try {
            EmployerDAO dao = EmployerDAO.getInstance();
            
            // Check current state
            List<Employer> employers = dao.list();
            System.out.println("Current employers in database: " + employers.size());
            
            for (int i = 0; i < employers.size(); i++) {
                Employer emp = employers.get(i);
                System.out.println("  " + (i+1) + ". ID: " + emp.getId() + 
                                   ", Name: " + emp.getFname() + " " + emp.getLname());
            }
            
            // Try to create a new employer
            System.out.println("\n--- Testing New Employer Creation ---");
            EmployerBuilder builder = new EmployerBuilder();
            builder.setFname("TestUser");
            builder.setLname("Current" + System.currentTimeMillis());
            builder.setDescription("Testing if save works - " + new java.util.Date());
            
            List<String> phones = new ArrayList<>();
            phones.add("555-0123");
            builder.setTel(phones);
            
            Employer testEmployer = builder.build();
            
            System.out.println("Attempting to create employer: " + testEmployer.getFname() + " " + testEmployer.getLname());
            
            boolean saved = dao.create(testEmployer);
            System.out.println("Save result: " + saved);
            
            if (saved) {
                // Check if it was actually saved
                List<Employer> afterSave = dao.list();
                System.out.println("Employers after save: " + afterSave.size());
                
                // Find our new employer
                Employer found = null;
                for (Employer emp : afterSave) {
                    if (emp.getFname().equals("TestUser") && emp.getLname().equals(testEmployer.getLname())) {
                        found = emp;
                        break;
                    }
                }
                
                if (found != null) {
                    System.out.println("✓ SUCCESS: New employer found in database with ID: " + found.getId());
                } else {
                    System.out.println("✗ ERROR: New employer not found in database after save!");
                }
            } else {
                System.out.println("✗ ERROR: DAO.create() returned false - save failed!");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}