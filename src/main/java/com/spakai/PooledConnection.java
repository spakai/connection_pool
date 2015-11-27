package com.spakai;

import java.time.Instant;

public class PooledConnection {
	
	JDBConnection connection;
	
	private boolean inUse = false;
	
	private Instant timeOfLease; 
	
	public PooledConnection(JDBConnection connection) {
		this.connection = connection;
	}
	
	public boolean isExpired() {
		return false;
	}
	
	public JDBConnection getConnection() {
		inUse = true;
		timeOfLease = Instant.now();
		return connection;
	}
}
