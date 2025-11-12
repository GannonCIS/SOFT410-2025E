package com.cbozan.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;


public class DB {

	public static String ERROR_MESSAGE = "";
	private static boolean errorShown = false; // Track if we already showed error dialog
	private static boolean initialized = false; // Track if DB schema is initialized
	
	private DB() {}
	private Connection conn = null;
	private static final Connection NOOP_CONNECTION = createNoopConnection();
	
	private static class DBHelper{
		private static final DB CONNECTION = new DB();
	}
	
	public static Connection getConnection() {
		return DBHelper.CONNECTION.connect();
	}
	
	public static void destroyConnection() {
		DBHelper.CONNECTION.disconnect();
	}
	
	public static boolean isUsingH2() {
		return DBConfig.DB_TYPE.equals("H2");
	}

	private Connection connect() {
		
		try {
			// Load database driver
			Class.forName(DBConfig.getDriverClass());
			
			if(conn == null || conn.isClosed()) {
				try {
					conn = DriverManager.getConnection(
						DBConfig.getJdbcUrl(), 
						DBConfig.getUsername(), 
						DBConfig.getPassword()
					);
					
					// Initialize database schema on first connection (H2 only)
					if (!initialized && DBConfig.DB_TYPE.equals("H2")) {
						initializeSchema(conn);
						initialized = true;
					}
					
					// Connection successful - reset error flag
					errorShown = false;
					ERROR_MESSAGE = "";
					System.out.println("✅ Database connected successfully (" + DBConfig.DB_TYPE + ")");
					
				} catch (SQLException e) {
					System.err.println("Database connection error: " + e.getMessage());
					ERROR_MESSAGE = "Cannot connect to database: " + e.getMessage();
					
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
			System.err.println("JDBC Driver not found: " + e.getMessage());
			ERROR_MESSAGE = "JDBC Driver not found in classpath.";
			
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
	
	/**
	 * Initialize H2 database schema with all required tables
	 */
	private void initializeSchema(Connection connection) {
		try (Statement stmt = connection.createStatement()) {
			System.out.println("🔧 Initializing database schema...");
			
			// Create tables in correct order (respecting foreign keys)
			
			// 1. Admin table
			stmt.execute("CREATE TABLE IF NOT EXISTS admin (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"username VARCHAR(100) NOT NULL UNIQUE, " +
				"password VARCHAR(255) NOT NULL, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// 2. Employer table
			stmt.execute("CREATE TABLE IF NOT EXISTS employer (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"fname VARCHAR(100) NOT NULL, " +
				"lname VARCHAR(100) NOT NULL, " +
				"tel VARCHAR(1000), " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// 3. Worker table
			stmt.execute("CREATE TABLE IF NOT EXISTS worker (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"fname VARCHAR(100) NOT NULL, " +
				"lname VARCHAR(100) NOT NULL, " +
				"tel VARCHAR(1000), " +
				"iban VARCHAR(34), " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// 4. Price table
			stmt.execute("CREATE TABLE IF NOT EXISTS price (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"fulltime DECIMAL(10,2) NOT NULL, " +
				"halftime DECIMAL(10,2) NOT NULL, " +
				"overtime DECIMAL(10,2) NOT NULL, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// 5. Worktype table
			stmt.execute("CREATE TABLE IF NOT EXISTS worktype (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"title VARCHAR(100) NOT NULL UNIQUE, " +
				"no INTEGER NOT NULL, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// 6. Paytype table
			stmt.execute("CREATE TABLE IF NOT EXISTS paytype (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"title VARCHAR(100) NOT NULL UNIQUE, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			
			// 7. Job table (depends on employer, price)
			stmt.execute("CREATE TABLE IF NOT EXISTS job (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"employer_id INTEGER NOT NULL, " +
				"price_id INTEGER NOT NULL, " +
				"title VARCHAR(200) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (employer_id) REFERENCES employer(id) ON DELETE CASCADE, " +
				"FOREIGN KEY (price_id) REFERENCES price(id))");
			
			// 8. Workgroup table (depends on job, worktype)
			stmt.execute("CREATE TABLE IF NOT EXISTS workgroup (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"job_id INTEGER NOT NULL, " +
				"worktype_id INTEGER NOT NULL, " +
				"quantity INTEGER NOT NULL, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (job_id) REFERENCES job(id) ON DELETE CASCADE, " +
				"FOREIGN KEY (worktype_id) REFERENCES worktype(id))");
			
			// 9. Work table (depends on worker, workgroup)
			// Note: 'day' is a reserved word in H2, so we quote it
			stmt.execute("CREATE TABLE IF NOT EXISTS work (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"worker_id INTEGER NOT NULL, " +
				"workgroup_id INTEGER NOT NULL, " +
				"\"day\" DECIMAL(5,2) NOT NULL, " +
				"description CLOB, " +
				"\"date\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (worker_id) REFERENCES worker(id) ON DELETE CASCADE, " +
				"FOREIGN KEY (workgroup_id) REFERENCES workgroup(id) ON DELETE CASCADE)");
			
			// 10. Payment table (depends on worker, paytype)
			stmt.execute("CREATE TABLE IF NOT EXISTS payment (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"worker_id INTEGER NOT NULL, " +
				"paytype_id INTEGER NOT NULL, " +
				"amount DECIMAL(10,2) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (worker_id) REFERENCES worker(id) ON DELETE CASCADE, " +
				"FOREIGN KEY (paytype_id) REFERENCES paytype(id))");
			
			// 11. Invoice table (depends on job)
			stmt.execute("CREATE TABLE IF NOT EXISTS invoice (" +
				"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
				"job_id INTEGER NOT NULL, " +
				"price DECIMAL(10,2) NOT NULL, " +
				"description CLOB, " +
				"date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (job_id) REFERENCES job(id) ON DELETE CASCADE)");
			
			// Insert default admin user if not exists
			stmt.execute("MERGE INTO admin (username, password) KEY(username) VALUES ('admin', 'admin')");
			
			// Insert sample data if tables are empty
			insertSampleDataIfEmpty(stmt);

			// Ensure requested demo records exist
			insertRequestedDemoRecords(connection, stmt);
			
			System.out.println("✅ Database schema initialized successfully!");
			
		} catch (SQLException e) {
			System.err.println("❌ Error initializing database schema: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Insert the specific demo records requested by the user, idempotently.
	 */
	private void insertRequestedDemoRecords(Connection connection, Statement stmt) throws SQLException {
		System.out.println("🧪 Ensuring requested demo records exist...");
		boolean isH2 = DBConfig.DB_TYPE.equals("H2");
		
		// Employer demo: ("Md Anisur Rahman Chowdhury", "8147375770", "CEO")
		String employerExistsSql = "SELECT COUNT(*) FROM employer WHERE fname='Md Anisur Rahman' AND lname='Chowdhury'";
		var rs = stmt.executeQuery(employerExistsSql);
		rs.next();
		if (rs.getInt(1) == 0) {
			if (isH2) {
				stmt.execute("INSERT INTO employer (fname, lname, tel, description) VALUES (" +
					"'Md Anisur Rahman', 'Chowdhury', '8147375770', 'CEO')");
			} else {
				// PostgreSQL: tel is VARCHAR[]
				stmt.execute("INSERT INTO employer (fname, lname, tel, description) VALUES (" +
					"'Md Anisur Rahman', 'Chowdhury', ARRAY['8147375770']::varchar[], 'CEO')");
			}
			System.out.println("✅ Demo Employer added: Md Anisur Rahman Chowdhury");
		}

		// Worker demo: use same details
		String workerExistsSql = "SELECT COUNT(*) FROM worker WHERE fname='Md Anisur Rahman' AND lname='Chowdhury'";
		rs = stmt.executeQuery(workerExistsSql);
		rs.next();
		if (rs.getInt(1) == 0) {
			if (isH2) {
				stmt.execute("INSERT INTO worker (fname, lname, tel, iban, description) VALUES (" +
					"'Md Anisur Rahman', 'Chowdhury', '8147375770', NULL, 'CEO')");
			} else {
				stmt.execute("INSERT INTO worker (fname, lname, tel, iban, description) VALUES (" +
					"'Md Anisur Rahman', 'Chowdhury', ARRAY['8147375770']::varchar[], NULL, 'CEO')");
			}
			System.out.println("✅ Demo Worker added: Md Anisur Rahman Chowdhury");
		}
	}
	
	/**
	 * Insert sample data to help user test the application immediately
	 */
	private void insertSampleDataIfEmpty(Statement stmt) throws SQLException {
		// Check if we already have data
		var rs = stmt.executeQuery("SELECT COUNT(*) FROM employer");
		rs.next();
		if (rs.getInt(1) > 0) {
			System.out.println("ℹ️ Sample data already exists, skipping...");
			return;
		}
		
		System.out.println("📝 Inserting sample data...");
		
		// Sample employers
		stmt.execute("INSERT INTO employer (fname, lname, tel, description) VALUES " +
			"('ABC', 'Construction', 'TEL:555-0001;CELL:555-0002', 'Large construction company'), " +
			"('Tech', 'Solutions', 'TEL:555-0010', 'IT services provider'), " +
			"('Green', 'Energy', 'TEL:555-0020;CELL:555-0021', 'Renewable energy projects')");
		
		// Sample workers
		stmt.execute("INSERT INTO worker (fname, lname, tel, iban, description) VALUES " +
			"('John', 'Smith', 'CELL:555-1001', 'TR330006100519786457841326', 'Experienced carpenter'), " +
			"('Mary', 'Johnson', 'CELL:555-1002', 'TR330006100519786457841327', 'Electrician'), " +
			"('David', 'Williams', 'CELL:555-1003', 'TR330006100519786457841328', 'Plumber'), " +
			"('Sarah', 'Brown', 'CELL:555-1004', 'TR330006100519786457841329', 'Painter')");
		
		// Sample prices
		stmt.execute("INSERT INTO price (fulltime, halftime, overtime) VALUES " +
			"(500.00, 250.00, 750.00), " +
			"(600.00, 300.00, 900.00), " +
			"(450.00, 225.00, 675.00)");
		
		// Sample worktypes
		stmt.execute("INSERT INTO worktype (title, no) VALUES " +
			"('Carpentry', 1), " +
			"('Electrical', 2), " +
			"('Plumbing', 3), " +
			"('Painting', 4)");
		
		// Sample paytypes
		stmt.execute("INSERT INTO paytype (title) VALUES " +
			"('Cash'), " +
			"('Bank Transfer'), " +
			"('Check')");
		
		// Sample jobs
		stmt.execute("INSERT INTO job (employer_id, price_id, title, description) VALUES " +
			"(1, 1, 'Building Renovation', 'Complete renovation of office building'), " +
			"(2, 2, 'Network Installation', 'Install network infrastructure'), " +
			"(3, 3, 'Solar Panel Installation', 'Install solar panels on warehouse roof')");
		
		System.out.println("✅ Sample data inserted successfully!");
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
	
	private static void showDatabaseError() {
		String message = "⚠️ DATABASE ERROR ⚠️\n\n" +
				"Failed to connect to database.\n\n";
		
		if (DBConfig.DB_TYPE.equals("H2")) {
			message += "H2 Database Configuration:\n" +
				"• Database file: ./data/hesap-eproject\n" +
				"• This should work automatically!\n\n" +
				"Error: " + ERROR_MESSAGE + "\n\n" +
				"Please check:\n" +
				"1. Do you have write permissions in this folder?\n" +
				"2. Is the data folder accessible?\n" +
				"3. Check the console for detailed error messages.";
		} else {
			message += "PostgreSQL Configuration:\n" +
				"Database: Hesap-eProject\n" +
				"Username: Hesap-eProject\n" +
				"Password: .hesap-eProject.\n\n" +
				"Please ensure:\n" +
				"1. PostgreSQL is installed and running\n" +
				"2. Database 'Hesap-eProject' exists\n" +
				"3. User has proper permissions\n\n" +
				"Run: cd database && setup-database.bat\n\n" +
				"To use H2 instead (no installation needed):\n" +
				"Change DB_TYPE to 'H2' in DBConfig.java";
		}
		
		JOptionPane.showMessageDialog(null, message, "Database Error", JOptionPane.ERROR_MESSAGE);
	}
	
}