package com.spakai;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.junit.Assert.assertEquals;

public class ConnectionPoolTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();
	 
  @Mock private JdbConnection mockJDBConnection;
  @Mock private JdbConnectionFactory mockConnectionFactory;
	
  private final long shortLeaseLife = 1000;
  private final long longLeaseLife  = 10000;
  private final int poolSize         = 5;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void FactoryCreatesConnectionsOnCreation() {
    given(mockConnectionFactory.create()).willReturn(mockJDBConnection);
    new ConnectionPool(mockConnectionFactory, poolSize, shortLeaseLife);
    then(mockConnectionFactory).should(times(poolSize)).create();
  }

	@Test
	public void BorrowingReturnsTheOldestConnection() throws ConnectionPoolException {
		given(mockConnectionFactory.create()).willReturn(mockJDBConnection);
		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, 0, shortLeaseLife);
		
		JdbConnection oldMockJDBConnection = mock(JdbConnection.class);
		JdbConnection olderMockJDBConnection = mock(JdbConnection.class);
		pool.forfeit(olderMockJDBConnection);
		pool.forfeit(oldMockJDBConnection);
		
		assertEquals(pool.borrow(),olderMockJDBConnection);
		assertEquals(pool.borrow(),oldMockJDBConnection);
	}
	
	@Test
	public void BorrowingReturnsAReplacementConnection() throws InterruptedException,  ConnectionPoolException {
		given(mockConnectionFactory.create()).willReturn(mockJDBConnection);
		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, shortLeaseLife);
		
		//borrow them all but don't return
		for(int i=0; i< poolSize; i++) {
			pool.borrow();
		}
		
		//and now let them expire
		Thread.sleep(shortLeaseLife + 1000);
		pool.borrow();
		
		then(mockJDBConnection).should(times(1)).close();
		then(mockConnectionFactory).should(times(poolSize+1)).create();
	}
	
	@Test
	public void BorrowingNotPossibleAsAllConnectionsAreInUse() throws InterruptedException,  ConnectionPoolException {
		thrown.expect(ConnectionPoolException.class);
		thrown.expectMessage("No connections available");
		
		given(mockConnectionFactory.create()).willReturn(mockJDBConnection);
		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, longLeaseLife);
		
		//borrow them all but don't return
		for(int i=0; i< poolSize; i++) {
			pool.borrow();
		}
		
		//borrow again before the others expire
		pool.borrow();
	}
	
	@Test
	public void ReturningAConnection() throws InterruptedException, ConnectionPoolException {
		given(mockConnectionFactory.create()).willReturn(mockJDBConnection);
		ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, shortLeaseLife);
		
		JdbConnection pc1 = pool.borrow();
		pool.forfeit(pc1);
		
		assertEquals(pool.borrow(), pc1);
	}
}
