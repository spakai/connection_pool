package com.spakai;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;

import org.junit.Test;
import org.junit.Before;

public class ConnectionPoolTest {
	@Mock JDBConnection mockConnection;
	@Mock JDBConnectionFactory mockConnectionFactory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void FactoryCreatesConnectionsOnCreation() {
		when(mockConnectionFactory.create()).thenReturn(mockConnection);
		long timeOfLease = 1000;
		int poolSize     = 5;

		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, timeOfLease);

		verify(mockConnectionFactory, times(poolSize)).create();
	}
}
