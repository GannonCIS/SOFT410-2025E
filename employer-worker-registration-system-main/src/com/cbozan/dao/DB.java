package com.cbozan.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DB {

	public static String ERROR_MESSAGE = "";
	private static boolean offline = false;
	private static boolean usingH2 = false;
	
	private DB() {}
	private Connection conn = null;
	
	private static class DBHelper{
		private static final DB CONNECTION = new DB();
	}
	
	public static Connection getConnection() {
		return DBHelper.CONNECTION.connect();
	}
	
	public static void destroyConnection() {
		DBHelper.CONNECTION.disconnect();
	}
	
	public static boolean isOffline() {
		return offline;
	}
	
	public static boolean isUsingH2() {
		return usingH2;
	}

	private Connection connect() {
		
		try {
			if(conn == null || conn.isClosed()) {
				try {
					// First try PostgreSQL
					conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Hesap-eProject", "Hesap-eProject", ".hesap-eProject.");
					offline = false;
					usingH2 = false;
					ERROR_MESSAGE = "";
					System.out.println("Connected to PostgreSQL database");
				} catch (SQLException e) {
					// PostgreSQL failed, try H2 embedded database
					try {
						conn = DriverManager.getConnection("jdbc:h2:~/hesap-eproject;AUTO_SERVER=TRUE", "sa", "");
						offline = false;
						usingH2 = true;
						ERROR_MESSAGE = "Using H2 embedded database";
						System.out.println("Connected to H2 embedded database");
						// Initialize database schema for H2
						initializeH2Schema();
					} catch (SQLException h2e) {
						// Both PostgreSQL and H2 failed — run in offline mode
						offline = true;
						usingH2 = false;
						ERROR_MESSAGE = "Running in offline mode (no DB). Some features will be disabled.";
						System.err.println("PostgreSQL error: " + e.getMessage());
						System.err.println("H2 error: " + h2e.getMessage());
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	private void initializeH2Schema() {
		try (Statement stmt = conn.createStatement()) {
			// Create tables if they don't exist (H2 syntax)
			
			// Employer table
			stmt.execute("CREATE TABLE IF NOT EXISTS employer (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"fname VARCHAR(50) NOT NULL, " +
				"lname VARCHAR(50) NOT NULL, " +
				"tel CLOB, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
				")");
			
			// Worker table
			stmt.execute("CREATE TABLE IF NOT EXISTS worker (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"fname VARCHAR(50) NOT NULL, " +
				"lname VARCHAR(50) NOT NULL, " +
				"tel CLOB, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
				")");
			
			// Job table
			stmt.execute("CREATE TABLE IF NOT EXISTS job (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"employer_id INT NOT NULL, " +
				"name VARCHAR(100) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (employer_id) REFERENCES employer(id)" +
				")");
			
			// Worktype table
			stmt.execute("CREATE TABLE IF NOT EXISTS worktype (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"name VARCHAR(50) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
				")");
			
			// Workgroup table
			stmt.execute("CREATE TABLE IF NOT EXISTS workgroup (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"name VARCHAR(50) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
				")");
			
			// Work table
			stmt.execute("CREATE TABLE IF NOT EXISTS work (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"job_id INT NOT NULL, " +
				"worker_id INT NOT NULL, " +
				"worktype_id INT NOT NULL, " +
				"workgroup_id INT NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (job_id) REFERENCES job(id), " +
				"FOREIGN KEY (worker_id) REFERENCES worker(id), " +
				"FOREIGN KEY (worktype_id) REFERENCES worktype(id), " +
				"FOREIGN KEY (workgroup_id) REFERENCES workgroup(id)" +
				")");
			
			// Paytype table
			stmt.execute("CREATE TABLE IF NOT EXISTS paytype (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"name VARCHAR(50) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
				")");
			
			// Price table
			stmt.execute("CREATE TABLE IF NOT EXISTS price (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"worktype_id INT NOT NULL, " +
				"paytype_id INT NOT NULL, " +
				"amount DECIMAL(10,2) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (worktype_id) REFERENCES worktype(id), " +
				"FOREIGN KEY (paytype_id) REFERENCES paytype(id)" +
				")");
			
			// Payment table
			stmt.execute("CREATE TABLE IF NOT EXISTS payment (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"worker_id INT NOT NULL, " +
				"job_id INT, " +
				"paytype_id INT NOT NULL, " +
				"amount DECIMAL(10,2) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (worker_id) REFERENCES worker(id), " +
				"FOREIGN KEY (job_id) REFERENCES job(id), " +
				"FOREIGN KEY (paytype_id) REFERENCES paytype(id)" +
				")");
			
			// Invoice table
			stmt.execute("CREATE TABLE IF NOT EXISTS invoice (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"job_id INT NOT NULL, " +
				"amount DECIMAL(10,2) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (job_id) REFERENCES job(id)" +
				")");
			
			System.out.println("H2 database schema initialized successfully");
			
		} catch (SQLException e) {
			System.err.println("Failed to initialize H2 schema: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void disconnect() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
	}

}

