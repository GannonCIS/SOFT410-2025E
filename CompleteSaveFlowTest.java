import com.cbozan.dao.EmployerDAO;
import com.cbozan.entity.Employer;
import java.util.List;
import java.util.ArrayList;

public class CompleteSaveFlowTest {
    public static void main(String[] args) {
        System.out.println("=== Complete Save Flow Test ===");
        System.out.println("This simulates exactly what happens when you click 'Save' in the GUI\n");
        
        try {
            EmployerDAO dao = EmployerDAO.getInstance();
            
            // Step 1: Check initial state
            System.out.println("STEP 1: Initial database state");
            List<Employer> before = dao.list();
            System.out.println("   Employers before save: " + before.size());
            
            // Step 2: Simulate the exact save process from EmployerPanel
            System.out.println("\nSTEP 2: Simulating GUI save process...");
            System.out.println("   Creating employer using same builder as GUI...");
            
            Employer.EmployerBuilder builder = new Employer.EmployerBuilder();
            builder.setId(Integer.MAX_VALUE);
            builder.setFname("GUI");
            builder.setLname("TEST123"); // Keep under 20 character limit
            
            // Simulate phone number validation (Turkish format required)
            List<String> phones = new ArrayList<>();
            phones.add("05551234567"); // Valid Turkish format
            builder.setTel(phones);
            
            builder.setDescription("Simulating exact GUI save flow");
            
            Employer testEmployer = builder.build();
            System.out.println("   Built employer: " + testEmployer.getFname() + " " + testEmployer.getLname());
            
            // Step 3: Save using DAO (same as GUI)
            System.out.println("\nSTEP 3: Saving via DAO.create()...");
            boolean saveResult = dao.create(testEmployer);
            
            if (saveResult) {
                System.out.println("   ✓ DAO.create() returned TRUE - save successful!");
                System.out.println("   (In GUI: You would see 'Registration Successful' message)");
            } else {
                System.out.println("   ✗ DAO.create() returned FALSE - save failed!");
                System.out.println("   (In GUI: You would see 'DataBase error' message)");
                return;
            }
            
            // Step 4: Check if data is in database
            System.out.println("\nSTEP 4: Verifying database contains new data...");
            List<Employer> after = dao.list();
            System.out.println("   Employers after save: " + after.size());
            
            if (after.size() > before.size()) {
                System.out.println("   ✓ SUCCESS: Database count increased!");
                
                // Find our new employer
                Employer found = null;
                for (Employer emp : after) {
                    if (emp.getFname().equals("GUI") && emp.getLname().startsWith("FLOWTEST")) {
                        found = emp;
                        break;
                    }
                }
                
                if (found != null) {
                    System.out.println("   ✓ SUCCESS: New employer found in database with ID: " + found.getId());
                } else {
                    System.out.println("   ✗ ERROR: New employer not found in database results!");
                }
            } else {
                System.out.println("   ✗ ERROR: Database count did not increase!");
            }
            
            // Step 5: Simulate display refresh (what should happen after save)
            System.out.println("\nSTEP 5: Simulating display refresh...");
            dao.refreshCache();
            List<Employer> displayList = dao.list();
            System.out.println("   Display would show: " + displayList.size() + " employers");
            
            // Check if display would show new data
            boolean displayWouldShow = false;
            for (Employer emp : displayList) {
                if (emp.getFname().equals("GUI") && emp.getLname().startsWith("FLOWTEST")) {
                    displayWouldShow = true;
                    System.out.println("   ✓ SUCCESS: Display would show new employer: " + 
                                     emp.getFname() + " " + emp.getLname());
                    break;
                }
            }
            
            if (!displayWouldShow) {
                System.out.println("   ✗ ERROR: Display would NOT show new employer!");
            }
            
            System.out.println("\n=== CONCLUSION ===");
            if (saveResult && displayWouldShow) {
                System.out.println("✓ COMPLETE SUCCESS: Save flow works perfectly!");
                System.out.println("  - Data saves to database correctly");
                System.out.println("  - DAO returns updated data");
                System.out.println("  - Display should refresh correctly");
                System.out.println("\nIf GUI doesn't show data, the issue is in the observer pattern connection.");
            } else {
                System.out.println("✗ ISSUE FOUND in save flow!");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR in save flow: " + e.getMessage());
            e.printStackTrace();
        }
    }
}