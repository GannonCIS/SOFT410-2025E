import com.cbozan.dao.EmployerDAO;
import com.cbozan.entity.Employer;
import java.util.List;

public class CacheDebugTest {
    public static void main(String[] args) {
        System.out.println("=== Cache Debug Test ===");
        
        try {
            EmployerDAO dao = EmployerDAO.getInstance();
            
            System.out.println("1. Getting initial list from DAO...");
            List<Employer> initialList = dao.list();
            System.out.println("   Initial count: " + initialList.size());
            
            System.out.println("\n2. Forcing cache refresh...");
            dao.refreshCache();
            
            System.out.println("3. Getting list after cache refresh...");
            List<Employer> refreshedList = dao.list();
            System.out.println("   After refresh count: " + refreshedList.size());
            
            System.out.println("\n4. Recent employers from DAO:");
            for (int i = 0; i < Math.min(5, refreshedList.size()); i++) {
                Employer emp = refreshedList.get(refreshedList.size() - 1 - i); // Get from end
                System.out.println("   " + (i+1) + ". ID: " + emp.getId() + 
                                   ", Name: " + emp.getFname() + " " + emp.getLname() +
                                   ", Phone: " + emp.getTel());
            }
            
            System.out.println("\n5. Testing if cache is working correctly...");
            if (refreshedList.size() == 8) {
                System.out.println("✓ SUCCESS: DAO returns correct count (8 employers)");
                
                // Check if latest employer is there
                boolean foundLatest = false;
                for (Employer emp : refreshedList) {
                    if (emp.getId() == 8) {
                        foundLatest = true;
                        System.out.println("✓ SUCCESS: Found latest employer (ID=8): " + 
                                         emp.getFname() + " " + emp.getLname());
                        break;
                    }
                }
                
                if (!foundLatest) {
                    System.out.println("✗ ERROR: Latest employer (ID=8) not found in DAO cache!");
                }
            } else {
                System.out.println("✗ ERROR: DAO returns wrong count. Expected 8, got " + refreshedList.size());
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}