package com.spakai;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;

import org.junit.Test;
import org.junit.Before;

public class PooledConnectionTest {
	@Mock JDBConnection mockConnection;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void LeaseTimeExpired() throws java.lang.InterruptedException{
		PooledConnection pooledConn = new PooledConnection(mockConnection, 1000);
		try {
			pooledConn.getConnection();
		} catch (PooledConnectionException e) {
			fail();
		}
		Thread.sleep(2000);

		assertThat(pooledConn.isExpired(), is(true));
	}

	@Test
	public void LeaseTimeStillValid() throws java.lang.InterruptedException{
		PooledConnection pooledConn = new PooledConnection(mockConnection, 5000);
		try {
			pooledConn.getConnection();
		} catch (PooledConnectionException e) {
			fail();
		}
		Thread.sleep(2000);

		assertThat(pooledConn.isExpired(), is(false));
	}

}
