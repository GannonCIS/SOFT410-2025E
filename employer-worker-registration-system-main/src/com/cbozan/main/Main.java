package com.cbozan.main;

import java.awt.EventQueue;
import java.util.Locale;

import com.cbozan.util.DatabaseInitializer;

public class Main {
	
	public static void main(String[] args) {
        
	// Use language tag to avoid deprecated Locale constructors
	Locale.setDefault(Locale.forLanguageTag("tr-TR"));
	
		// Initialize database and sample data
		System.out.println("Initializing database...");
		DatabaseInitializer.initializeSampleData();
		
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				new Login();
				
			}
		});
			
	}
	
}
