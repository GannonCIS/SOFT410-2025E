import com.cbozan.view.helper.Control;

public class PhoneValidationTest {
    public static void main(String[] args) {
        System.out.println("=== Phone Number Validation Test ===");
        
        String[] testNumbers = {
            "",                     // Empty - should be valid
            "05551234567",          // Turkish mobile - should be valid
            "90551234567",          // Turkish without + - should be valid
            "+905551234567",        // Turkish with + - should be valid
            "555-123-4567",         // US format - should be invalid
            "123456789",            // Too short - should be invalid
            "5551234567",           // 10 digits - should be invalid for Turkish
            "abc123",               // Invalid - should be invalid
        };
        
        for (String number : testNumbers) {
            boolean isValid = Control.phoneNumberControl(number);
            System.out.printf("Phone: '%-15s' -> %s%n", number, isValid ? "VALID" : "INVALID");
        }
        
        System.out.println("\n=== Turkish Phone Format ===");
        System.out.println("Valid formats:");
        System.out.println("- Empty string (optional)");
        System.out.println("- +905551234567 (11 digits with +90)");
        System.out.println("- 905551234567 (11 digits with 90)");
        System.out.println("- 05551234567 (11 digits with 0)");
        
        System.out.println("\nIf you're having trouble, try using format: 05551234567");
        System.out.println("Or leave the phone field empty if it's optional.");
    }
}