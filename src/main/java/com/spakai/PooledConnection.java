package com.spakai;

import java.time.Instant;
import java.time.Duration;
import java.sql.SQLException;

// I need a class to keep track of how long the client has borrowed  JDBConnection object,
// which has the actual sql jdbc connection object . I chose composition over inheritance
// by having the PooledConnection class.

public class PooledConnection {

    JDBConnection connection;

	private boolean inUse = false;

	private long maxLeaseTimeInMillis=0;

	private Instant timeOfLease;

	public PooledConnection(JDBConnection connection, long maxLeaseTimeInMillis) {
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
		maxLeaseTimeInMillis = 0;
	}

    public JDBConnection getConnection() throws PooledConnectionException {
        if(inUse) {
            throw new PooledConnectionException("Connection already in use");
        }

        inUse = true;
        timeOfLease = Instant.now();
        return connection;
    }
}
