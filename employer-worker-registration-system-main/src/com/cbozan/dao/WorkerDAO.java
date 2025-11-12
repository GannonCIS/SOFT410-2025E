package com.cbozan.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.cbozan.entity.Worker;
import com.cbozan.entity.Worker.WorkerBuilder;
import com.cbozan.exception.EntityException;

public class WorkerDAO {
	
	private final HashMap<Integer, Worker> cache = new HashMap<>();
	private boolean usingCache = true;
	
	private WorkerDAO() {}
	
	// Read by id
	public Worker findById(int id) {
		
		if(usingCache == false)
			list();
		
		if(cache.containsKey(id))
			return cache.get(id);
		return null;
		
	}
	
	public void refresh() {
		setUsingCache(false);
		list();
		setUsingCache(true);
	}
	
	
	//Read All
	public List<Worker> list(){
		
		List<Worker> list = new ArrayList<>();

		if(DB.isOffline()) return list;
		
		if(cache.size() != 0 && usingCache) {
			for(Entry<Integer, Worker> obj : cache.entrySet()) {
				list.add(obj.getValue());
			}
			return list;
		}
		
		cache.clear();
		
		Connection conn;
		Statement st;
		ResultSet rs;
		String query = "SELECT * FROM worker;";
		
		try {
			conn = DB.getConnection();
			if(conn == null) return list;
			st = conn.createStatement();
			rs = st.executeQuery(query);
			
			WorkerBuilder builder;
			Worker worker;
			
			while(rs.next()) {
				
				builder = new WorkerBuilder();
				builder.setId(rs.getInt("id"));
				builder.setFname(rs.getString("fname"));
				builder.setLname(rs.getString("lname"));
				
				// Handle tel field - could be array (PostgreSQL) or comma-separated string (H2)
				String telString = rs.getString("tel");
				if(telString == null || telString.trim().isEmpty()) {
					builder.setTel(null);
				} else {
					// Split by comma and trim whitespace
					String[] telArray = telString.split(",");
					List<String> telList = new ArrayList<>();
					for(String tel : telArray) {
						String trimmed = tel.trim();
						if(!trimmed.isEmpty()) {
							telList.add(trimmed);
						}
					}
					builder.setTel(telList);
				}
				
				builder.setIban(rs.getString("iban"));
				builder.setDescription(rs.getString("description"));
				builder.setDate(rs.getTimestamp("date"));
				
				try {
					worker = builder.build();
					list.add(worker);
					cache.put(worker.getId(), worker);
				} catch (EntityException e) {
					showEntityException(e, rs.getString("fname") + " " + rs.getShort("lname"));
				}
				
			}
			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return list;
		
	}
	
	
	public boolean create(Worker worker) {
		
		if(createControl(worker) == false)
			return false;
		
		Connection conn;
		PreparedStatement pst;
		int result = 0;
		String query = "INSERT INTO worker (fname,lname,tel,iban,description) VALUES (?,?,?,?,?);";
		String query2 = "SELECT * FROM worker ORDER BY id DESC LIMIT 1;";
		
		try {
			conn = DB.getConnection();
			pst = conn.prepareStatement(query);
			pst.setString(1, worker.getFname());
			pst.setString(2, worker.getLname());
			
			// Handle tel field for both PostgreSQL (array) and H2 (string)
			if(worker.getTel() == null || worker.getTel().isEmpty()) {
				pst.setString(3, null);
			} else if(DB.isUsingH2()) {
				// H2: store as comma-separated string
				pst.setString(3, String.join(",", worker.getTel()));
			} else {
				// PostgreSQL: store as array
				java.sql.Array phones = conn.createArrayOf("VARCHAR", worker.getTel().toArray());
				pst.setArray(3, phones);
			}
			
			pst.setString(4, worker.getIban());
			pst.setString(5, worker.getDescription());
			result = pst.executeUpdate();
			
			if(result != 0) {
				
				ResultSet rs = conn.createStatement().executeQuery(query2);
				while(rs.next()) {
					
					WorkerBuilder builder = new WorkerBuilder();
					builder = new WorkerBuilder();
					builder.setId(rs.getInt("id"));
					builder.setFname(rs.getString("fname"));
					builder.setLname(rs.getString("lname"));
					
					if(rs.getArray("tel") == null)
						builder.setTel(null);
					else
						builder.setTel(Arrays.asList((String [])rs.getArray("tel").getArray()));
					
					builder.setIban(rs.getString("iban"));
					builder.setDescription(rs.getString("description"));
					builder.setDate(rs.getTimestamp("date"));
					
					try {
						Worker wor = builder.build();
						cache.put(wor.getId(), wor);
					} catch (EntityException e) {
						showEntityException(e, rs.getString("fname") + " " + rs.getShort("lname"));
					}
					
				}
				
			}
			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return result == 0 ? false : true;
	}
	
	private boolean createControl(Worker worker) {
		for(Entry<Integer, Worker> obj : cache.entrySet()) {
			if(obj.getValue().getFname().equals(worker.getFname()) && obj.getValue().getLname().equals(worker.getLname())) {
				DB.ERROR_MESSAGE = obj.getValue().getFname() + " " + obj.getValue().getLname() + " kaydı zaten mevcut.";
				return false;
			}
		}
		return true;
	}
	
	public boolean update(Worker worker) {
		if(updateControl(worker) == false)
			return false;
		
		Connection conn;
		PreparedStatement pst;
		int result = 0;
		String query = "UPDATE worker SET fname=?,"
				+ "lname=?, tel=?, iban=?, description=? WHERE id=?;";
		
		try {
			conn = DB.getConnection();
			pst = conn.prepareStatement(query);
			pst.setString(1, worker.getFname());
			pst.setString(2, worker.getLname());
			
			// Handle tel field for both PostgreSQL (array) and H2 (string)
			if(worker.getTel() == null || worker.getTel().isEmpty()) {
				pst.setString(3, null);
			} else if(DB.isUsingH2()) {
				// H2: store as comma-separated string
				pst.setString(3, String.join(",", worker.getTel()));
			} else {
				// PostgreSQL: store as array
				java.sql.Array phones = conn.createArrayOf("VARCHAR", worker.getTel().toArray());
				pst.setArray(3, phones);
			}
			
			pst.setString(4, worker.getIban());
			pst.setString(5, worker.getDescription());
			pst.setInt(6, worker.getId());
			
			result = pst.executeUpdate();
			
			if(result != 0) {
				cache.put(worker.getId(), worker);
			}
			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return result == 0 ? false : true;
	}
	
	private boolean updateControl(Worker worker) {
		for(Entry<Integer, Worker> obj : cache.entrySet()) {
			if(obj.getValue().getFname().equals(worker.getFname()) 
					&& obj.getValue().getLname().equals(worker.getLname())
					&& obj.getValue().getId() != worker.getId()) {
				DB.ERROR_MESSAGE = obj.getValue().getFname() + " " + obj.getValue().getLname() + " kaydı zaten mevcut.";
				return false;
			}
		}
		return true;
	}
	
	public boolean delete(Worker worker) {
		
		Connection conn;
		PreparedStatement ps;
		int result = 0;
		String query = "DELETE FROM worker WHERE id=?;";
		
		try {
			
			conn = DB.getConnection();
			ps = conn.prepareStatement(query);
			ps.setInt(1, worker.getId());
			result = ps.executeUpdate();
			
			if(result != 0) {
				cache.remove(worker.getId());
			}
			
			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return result == 0 ? false : true;
		
	}
	
	
	
	private static class WorkerDAOHelper {
		private static final WorkerDAO instance = new WorkerDAO();
	}
	
	public static WorkerDAO getInstance() {
		return WorkerDAOHelper.instance;
	}

	public boolean isUsingCache() {
		return usingCache;
	}

	public void setUsingCache(boolean usingCache) {
		this.usingCache = usingCache;
	}
	
	private void showEntityException(EntityException e, String msg) {
		String message = msg + " not added" + 
				"\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + e.getCause();
			JOptionPane.showMessageDialog(null, message);
	}
	
	private void showSQLException(SQLException e) {
		String message = e.getErrorCode() + "\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause();
		JOptionPane.showMessageDialog(null, message);
	}
}
