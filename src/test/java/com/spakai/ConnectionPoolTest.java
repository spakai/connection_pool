package com.spakai;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;

public class ConnectionPoolTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	 
	@Mock JDBConnection mockJDBConnection;
	@Mock JDBConnectionFactory mockConnectionFactory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void FactoryCreatesConnectionsOnCreation() {
		when(mockConnectionFactory.create()).thenReturn(mockJDBConnection);
		long timeOfLease = 1000;
		int poolSize     = 5;

		new ConnectionPool(mockConnectionFactory, poolSize, timeOfLease);

		verify(mockConnectionFactory, times(poolSize)).create();
	}

	@Test
	public void BorrowingReturnsTheOldestConnection() throws ConnectionPoolException {
		when(mockConnectionFactory.create()).thenReturn(mockJDBConnection);
		long timeOfLease = 1000;
		int poolSize     = 5;

		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, timeOfLease);
		
		assertThat(pool.borrow().getId(), is(0));
		assertThat(pool.borrow().getId(), is(1));
		assertThat(pool.borrow().getId(), is(2));
		
	}
	
	@Test
	public void BorrowingReturnsAReplacementConnection() throws InterruptedException, PooledConnectionException, ConnectionPoolException {
		when(mockConnectionFactory.create()).thenReturn(mockJDBConnection);
		long timeOfLease = 1000;
		int poolSize     = 5;

		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, timeOfLease);
		
		for(int i=0; i< poolSize; i++) {
			pool.borrow();
		}
		
		//Let them expire
		Thread.sleep(3000);
		PooledConnection pc = pool.borrow();
		
		verify(mockConnectionFactory, times(poolSize+1)).create();
		
		assertThat(pc.getId(), is(100));
		
	}
	
	@Test
	public void BorrowingNotPossibleAsAllConnectionsAreInUse() throws InterruptedException, PooledConnectionException, ConnectionPoolException {
		
		thrown.expect(ConnectionPoolException.class);
		thrown.expectMessage("No connections available");
		
		when(mockConnectionFactory.create()).thenReturn(mockJDBConnection);
		long timeOfLease = 1000;
		int poolSize     = 5;

		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, timeOfLease);
		
		for(int i=0; i< poolSize; i++) {
			pool.borrow();
		}
		
		pool.borrow();
		
	}
	
	@Test
	public void ReturningAConnection() throws InterruptedException, PooledConnectionException, ConnectionPoolException {
		
		when(mockConnectionFactory.create()).thenReturn(mockJDBConnection);
		long timeOfLease = 1000;
		int poolSize     = 1;

		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, timeOfLease);
		
		PooledConnection pc = pool.borrow();
		pool.forfeit(pc);
		
		assertThat(pool.borrow().getId(), is(0));
		
	}
}
