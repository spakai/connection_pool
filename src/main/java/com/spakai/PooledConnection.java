package com.spakai;

import java.time.Instant;
import java.time.Duration;

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
		Instant current = Instant.now();
		Duration timeElapsed = Duration.between(timeOfLease, current);
		if(timeElapsed.toMillis() > maxLeaseTimeInMillis)
			return true;

		return false;
	}

	public JDBConnection getConnection() {
		inUse = true;
		timeOfLease = Instant.now();
		return connection;
	}
}
