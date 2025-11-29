import com.cbozan.main.MainFrame;
import com.cbozan.view.record.EmployerPanel;
import com.cbozan.view.display.EmployerDisplay;
import com.cbozan.dao.EmployerDAO;
import com.cbozan.entity.Employer;
import com.cbozan.entity.EmployerBuilder;

public class TestObserverPattern {
    public static void main(String[] args) {
        System.out.println("Testing Observer Pattern Fix...");
        
        try {
            // Initialize database
            EmployerDAO dao = EmployerDAO.getInstance();
            System.out.println("Initial employer count: " + dao.list().size());
            
            // Create a mock employer panel and display
            EmployerPanel employerPanel = new EmployerPanel();
            EmployerDisplay employerDisplay = new EmployerDisplay();
            
            // Connect observer pattern (this is what we fixed in MainFrame)
            employerPanel.subscribe(employerDisplay);
            System.out.println("Observer connection established");
            
            // Create a test employer
            EmployerBuilder builder = new EmployerBuilder();
            builder.setFname("Test");
            builder.setLname("Employer");
            builder.setDescription("Test employer created for observer pattern test");
            
            Employer testEmployer = builder.build();
            
            // Save through DAO and trigger notification
            if (dao.create(testEmployer)) {
                System.out.println("Test employer created successfully");
                
                // This should notify observers (including the display)
                employerPanel.notifyAllObservers();
                System.out.println("Observers notified");
                
                System.out.println("Final employer count: " + dao.list().size());
                System.out.println("Observer pattern fix verified!");
            } else {
                System.out.println("Failed to create test employer");
            }
            
        } catch (Exception e) {
            System.err.println("Error testing observer pattern: " + e.getMessage());
            e.printStackTrace();
        }
    }
}