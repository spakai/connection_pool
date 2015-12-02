package com.spakai;

import java.time.Instant;
import java.time.Duration;
import java.sql.SQLException;

// I need a class to keep track of how long the client has borrowed  JDBConnection object,
// which has the actual sql jdbc connection object . I chose composition over inheritance
// by having the PooledConnection class.

public class PooledConnection {

    JDBConnection connection;

    private int id;
    
    private boolean inUse = false;

    private long maxLeaseTimeInMillis=Long.MAX_VALUE;
   
    private Instant timeOfLease;
    
    public PooledConnection(JDBConnection connection, long maxLeaseTimeInMillis) {
    	this.setId(0);
        this.connection = connection;
        this.maxLeaseTimeInMillis = maxLeaseTimeInMillis;
    }
    

    public PooledConnection(int id, JDBConnection connection, long maxLeaseTimeInMillis) {
    	this.setId(id);
        this.connection = connection;
        this.maxLeaseTimeInMillis = maxLeaseTimeInMillis;
    }

    public boolean isExpired() {
        Duration timeElapsed = Duration.between(timeOfLease, Instant.now());
        if(timeElapsed.toMillis() > maxLeaseTimeInMillis) {
            return true;
        }

        return false;
    }

    public boolean isActive () {
        try {
            return connection.getConnection().isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    public void reset() {
        inUse = false;
        maxLeaseTimeInMillis = Long.MAX_VALUE;
    }
    
    public void set() {
    	inUse = true;
    	timeOfLease = Instant.now();
    }
    
    public JDBConnection getConnection() throws PooledConnectionException {
        return connection;
    }
    
    public void closeConnection() {
    	if(connection != null) {
    		connection.close();
    	}
    }

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}
}
