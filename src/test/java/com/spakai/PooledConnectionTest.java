package com.spakai;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;

import org.junit.Test;

public class PooledConnectionTest {
	@Test
	public void LeaseTimeExpired() {
		JDBConnection mockConnection = mock(JDBConnection.class);
		PooledConnection pooledConn = new PooledConnection(mockConnection);

		assertThat(pooledConn.isExpired(), is(false));
	}
}
