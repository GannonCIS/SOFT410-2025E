package com.cbozan.test;

import com.cbozan.dao.EmployerDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify the application compiles and runs on Java 17
 */
public class BasicApplicationTest {
    
    @BeforeEach
    public void setUp() {
        // Set system properties for headless testing
        System.setProperty("java.awt.headless", "true");
        System.setProperty("TEST_MODE", "true");
    }
    
    @Test
    public void testEmployerDAOInstantiation() {
        // Test that we can instantiate the DAO without errors
        assertNotNull(EmployerDAO.getInstance(), "EmployerDAO should be instantiable");
    }
    
    @Test
    public void testJavaVersionCompatibility() {
        // Verify we're running on a supported Java version
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should be available");
        
        // Should work on Java 17+
        assertTrue(javaVersion.startsWith("17") || 
                  javaVersion.startsWith("18") || 
                  javaVersion.startsWith("19") || 
                  javaVersion.startsWith("20") || 
                  javaVersion.startsWith("21") ||
                  javaVersion.startsWith("22") ||
                  javaVersion.startsWith("25"),
                  "Should run on Java 17 or higher, found: " + javaVersion);
    }
}