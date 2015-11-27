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
	public void LeaseTimeExpired() {
		PooledConnection pooledConn = new PooledConnection(mockConnection);

		assertThat(pooledConn.isExpired(), is(false));
	}
}
