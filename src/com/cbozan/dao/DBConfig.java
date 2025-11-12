package com.cbozan.dao;

/**
 * Database configuration settings
 * Switch between H2 (embedded) and PostgreSQL easily
 */
public class DBConfig {
    
    // Database type: "H2" or "POSTGRESQL"
    public static final String DB_TYPE = "H2"; // Change to "POSTGRESQL" when you install PostgreSQL
    
    // H2 Database settings (embedded - no installation needed)
    public static final String H2_URL = "jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE";
    public static final String H2_USER = "sa";
    public static final String H2_PASSWORD = "";
    
    // PostgreSQL settings
    public static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/Hesap-eProject";
    public static final String POSTGRES_USER = "Hesap-eProject";
    public static final String POSTGRES_PASSWORD = ".hesap-eProject.";
    
    /**
     * Get the appropriate JDBC URL based on DB_TYPE
     */
    public static String getJdbcUrl() {
        return DB_TYPE.equals("H2") ? H2_URL : POSTGRES_URL;
    }
    
    /**
     * Get the appropriate username based on DB_TYPE
     */
    public static String getUsername() {
        return DB_TYPE.equals("H2") ? H2_USER : POSTGRES_USER;
    }
    
    /**
     * Get the appropriate password based on DB_TYPE
     */
    public static String getPassword() {
        return DB_TYPE.equals("H2") ? H2_PASSWORD : POSTGRES_PASSWORD;
    }
    
    /**
     * Get the appropriate JDBC driver class based on DB_TYPE
     */
    public static String getDriverClass() {
        return DB_TYPE.equals("H2") ? "org.h2.Driver" : "org.postgresql.Driver";
    }
}
