package com.cbozan.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;


public class DB {

	public static String ERROR_MESSAGE = "";
	private static boolean errorShown = false; // Track if we already showed error dialog
	
	private DB() {}
	private Connection conn = null;
	private static final Connection NOOP_CONNECTION = createNoopConnection();
	
	private static class DBHelper{
		private static final DB CONNECTION = new DB();
	}
	
	public static Connection getConnection() {
		return DBHelper.CONNECTION.connect();
	}
	
	// Get a fresh connection for each operation to ensure proper transaction handling
	public static Connection getFreshConnection() throws SQLException {
		try {
			// Load H2 JDBC driver
			Class.forName("org.h2.Driver");
			
			String url = System.getProperty("DB_URL", "jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1");
			String user = System.getProperty("DB_USER", "sa");
			String pass = System.getProperty("DB_PASSWORD", "");
			Connection freshConn = DriverManager.getConnection(url, user, pass);
			freshConn.setAutoCommit(true); // Ensure auto-commit is enabled for data persistence
			return freshConn;
		} catch (ClassNotFoundException e) {
			throw new SQLException("H2 JDBC Driver not found: " + e.getMessage(), e);
		}
	}
	
	public static void destroyConnection() {
		DBHelper.CONNECTION.disconnect();
	}

	private Connection connect() {
		
		try {
			// Load H2 JDBC driver
			Class.forName("org.h2.Driver");
			
			if(conn == null || conn.isClosed()) {
				try {
					// Use H2 embedded database stored in ./data directory
					String url = System.getProperty("DB_URL", "jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1");
					String user = System.getProperty("DB_USER", "sa");
					String pass = System.getProperty("DB_PASSWORD", "");
					conn = DriverManager.getConnection(url, user, pass);
					
					// Enable auto-commit for H2 database to ensure data persistence
					conn.setAutoCommit(true);
					
					// Initialize database if needed
					initializeDatabase(conn);
					
					// Connection successful - reset error flag
					errorShown = false;
					ERROR_MESSAGE = "";
				} catch (SQLException e) {
					System.err.println("Database connection error: " + e.getMessage());
					ERROR_MESSAGE = "Cannot connect to H2 database: " + e.getMessage();
					
					// Show error dialog only once
					if (!errorShown) {
						errorShown = true;
						showDatabaseError();
					}
					
					// Provide a non-null fallback Connection so DAOs can gracefully handle via SQLException
					conn = NOOP_CONNECTION;
				}
			}
		} catch (ClassNotFoundException e) {
			System.err.println("H2 JDBC Driver not found: " + e.getMessage());
			ERROR_MESSAGE = "H2 JDBC Driver not found in classpath.";
			
			if (!errorShown) {
				errorShown = true;
				showDatabaseError();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	private void disconnect() {
		if(conn != null && conn != NOOP_CONNECTION) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private static Connection createNoopConnection() {
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				// Any method call on this connection will behave like a failing DB by throwing SQLException
				throw new SQLException("No database connection available. " + (ERROR_MESSAGE == null ? "" : ERROR_MESSAGE));
			}
		};
		return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class[] { Connection.class },
				handler
		);
	}
	
	private void initializeDatabase(Connection conn) throws SQLException {
		// Check if all required tables exist
		boolean allTablesExist = true;
		String[] requiredTables = {"ADMIN", "EMPLOYER", "WORKER", "PRICE", "WORKTYPE", "PAYTYPE", "JOB", "WORKGROUP", "WORK", "PAYMENT", "INVOICE"};
		
		try (java.sql.PreparedStatement ps = conn.prepareStatement(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?")) {
			for (String tableName : requiredTables) {
				ps.setString(1, tableName);
				try (java.sql.ResultSet rs = ps.executeQuery()) {
					if (!rs.next() || rs.getInt(1) == 0) {
						allTablesExist = false;
						System.out.println("Missing table: " + tableName);
						break;
					}
				}
			}
			if (allTablesExist) {
				System.out.println("All database tables exist, skipping initialization.");
				return; // All tables exist
			}
		}
		
		// Create all tables needed for the application
		try (java.sql.Statement stmt = conn.createStatement()) {
			System.out.println("Initializing H2 database with complete schema...");
			
			// Create admin table
			stmt.execute("CREATE TABLE IF NOT EXISTS admin (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"username VARCHAR(100) NOT NULL UNIQUE, " +
					"password VARCHAR(255) NOT NULL, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// Create employer table
			stmt.execute("CREATE TABLE IF NOT EXISTS employer (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"fname VARCHAR(100) NOT NULL, " +
					"lname VARCHAR(100) NOT NULL, " +
					"tel VARCHAR(500), " +
					"description TEXT, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// Create worker table
			stmt.execute("CREATE TABLE IF NOT EXISTS worker (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"fname VARCHAR(100) NOT NULL, " +
					"lname VARCHAR(100) NOT NULL, " +
					"tel VARCHAR(500), " +
					"iban VARCHAR(34), " +
					"description TEXT, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// Create price table
			stmt.execute("CREATE TABLE IF NOT EXISTS price (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"fulltime DECIMAL(10,2) NOT NULL, " +
					"halftime DECIMAL(10,2) NOT NULL, " +
					"overtime DECIMAL(10,2) NOT NULL, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// Create worktype table
			stmt.execute("CREATE TABLE IF NOT EXISTS worktype (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"title VARCHAR(100) NOT NULL UNIQUE, " +
					"no INTEGER NOT NULL, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// Create paytype table
			stmt.execute("CREATE TABLE IF NOT EXISTS paytype (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"title VARCHAR(100) NOT NULL UNIQUE, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// Create job table
			stmt.execute("CREATE TABLE IF NOT EXISTS job (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"employer_id INTEGER NOT NULL, " +
					"price_id INTEGER NOT NULL, " +
					"title VARCHAR(200) NOT NULL, " +
					"description TEXT, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
					"FOREIGN KEY (employer_id) REFERENCES employer(id) ON DELETE CASCADE, " +
					"FOREIGN KEY (price_id) REFERENCES price(id) ON DELETE RESTRICT)");
			
			// Create workgroup table
			stmt.execute("CREATE TABLE IF NOT EXISTS workgroup (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"job_id INTEGER NOT NULL, " +
					"worktype_id INTEGER NOT NULL, " +
					"workcount INTEGER NOT NULL DEFAULT 0, " +
					"description TEXT, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
					"FOREIGN KEY (job_id) REFERENCES job(id) ON DELETE CASCADE, " +
					"FOREIGN KEY (worktype_id) REFERENCES worktype(id) ON DELETE RESTRICT)");
			
			// Create work table (avoid reserved keyword 'day')
			stmt.execute("CREATE TABLE IF NOT EXISTS work (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"job_id INTEGER NOT NULL, " +
					"worker_id INTEGER NOT NULL, " +
					"worktype_id INTEGER NOT NULL, " +
					"workgroup_id INTEGER NOT NULL, " +
					"description TEXT, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
					"FOREIGN KEY (job_id) REFERENCES job(id) ON DELETE CASCADE, " +
					"FOREIGN KEY (worker_id) REFERENCES worker(id) ON DELETE CASCADE, " +
					"FOREIGN KEY (worktype_id) REFERENCES worktype(id) ON DELETE RESTRICT, " +
					"FOREIGN KEY (workgroup_id) REFERENCES workgroup(id) ON DELETE CASCADE)");
			
			// Create payment table
			stmt.execute("CREATE TABLE IF NOT EXISTS payment (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"worker_id INTEGER NOT NULL, " +
					"job_id INTEGER NOT NULL, " +
					"paytype_id INTEGER NOT NULL, " +
					"amount DECIMAL(10,2) NOT NULL, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
					"FOREIGN KEY (worker_id) REFERENCES worker(id) ON DELETE CASCADE, " +
					"FOREIGN KEY (job_id) REFERENCES job(id) ON DELETE CASCADE, " +
					"FOREIGN KEY (paytype_id) REFERENCES paytype(id) ON DELETE RESTRICT)");
			
			// Create invoice table
			stmt.execute("CREATE TABLE IF NOT EXISTS invoice (" +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
					"job_id INTEGER NOT NULL, " +
					"amount DECIMAL(10,2) NOT NULL, " +
					"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
					"FOREIGN KEY (job_id) REFERENCES job(id) ON DELETE CASCADE)");
			
			// Insert default data
			stmt.execute("INSERT INTO admin (username, password) VALUES ('admin', 'admin')");
			
			// Insert basic work types
			stmt.execute("INSERT INTO worktype (title, no) VALUES ('FULL TIME', 1)");
			stmt.execute("INSERT INTO worktype (title, no) VALUES ('HALF TIME', 2)");
			stmt.execute("INSERT INTO worktype (title, no) VALUES ('OVERTIME', 3)");
			
			// Insert basic payment types
			stmt.execute("INSERT INTO paytype (title) VALUES ('CASH')");
			stmt.execute("INSERT INTO paytype (title) VALUES ('BANK TRANSFER')");
			stmt.execute("INSERT INTO paytype (title) VALUES ('CHECK')");
			
			// Insert basic pricing
			stmt.execute("INSERT INTO price (fulltime, halftime, overtime) VALUES (100.00, 50.00, 150.00)");
			
			System.out.println("H2 database initialized successfully with complete schema!");
			System.out.println("Default login: admin / admin");
		}
	}

	private static void showDatabaseError() {
		// Skip GUI dialog in headless or test environments
		if (Boolean.getBoolean("TEST_MODE") || "true".equalsIgnoreCase(System.getenv("CI")) || java.awt.GraphicsEnvironment.isHeadless()) {
			System.err.println("Database Setup Required: running in headless/CI/test mode, suppressing dialog.");
			return;
		}
		String message = "⚠️ DATABASE NOT CONNECTED ⚠️\n\n" +
				"H2 database connection failed.\n\n" +
				"The application will use embedded H2 database.\n" +
				"Default login: admin / admin\n\n" +
				"Database will be created in ./data/ directory.";
		
		JOptionPane.showMessageDialog(null, message, "Database Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
}