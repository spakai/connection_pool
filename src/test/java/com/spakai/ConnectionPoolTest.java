package com.spakai;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Ignore;


public class ConnectionPoolTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @Mock private JdbConnection mockJdbConnection0;
  @Mock private JdbConnection mockJdbConnection1;
  @Mock private JdbConnection mockJdbConnection2;
  @Mock private JdbConnection mockJdbConnection3;
  @Mock private JdbConnection mockJdbConnection4;
  
  @Mock private JdbConnectionFactory mockConnectionFactory;

  private final long shortLeaseLife = 1000;
  private final long longLeaseLife  = 10000;
  private final int poolSize        = 5;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void factoryCreatesConnectionsOnCreation() {
    given(mockConnectionFactory.create()).willReturn(mockJdbConnection0);
    new ConnectionPool(mockConnectionFactory, poolSize, shortLeaseLife);
    then(mockConnectionFactory).should(times(poolSize)).create();
  }

  @Test
  @Ignore
  public void borrowingReturnsAReplacementConnection() 
    throws InterruptedException, ConnectionPoolException {
    given(mockConnectionFactory.create()).willReturn(mockJdbConnection0);
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, shortLeaseLife);

    //borrow them all but don't return
    for (int i = 0; i < poolSize; i++) {
      pool.borrow();
    }

    //and now let them expire
    Thread.sleep(shortLeaseLife + 1000);
    pool.borrow();

    then(mockJdbConnection0).should(times(1)).close();
    then(mockConnectionFactory).should(times(poolSize + 1)).create();
  }
  
  @Test
  public void borrowingReturnsTheOldestConnection() 
    throws InterruptedException, ConnectionPoolException {
	  given(mockConnectionFactory.create())
	    .willReturn(mockJdbConnection0)
	    .willReturn(mockJdbConnection1)
	    .willReturn(mockJdbConnection2)
	    .willReturn(mockJdbConnection3)
	    .willReturn(mockJdbConnection4);
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, longLeaseLife);

    assertThat(pool.borrow(), is(mockJdbConnection0));
  }

  @Test
  @Ignore
  public void borrowingNotPossibleAsAllConnectionsAreInUse() throws ConnectionPoolException {
    thrown.expect(ConnectionPoolException.class);
    thrown.expectMessage("No connections available");

    given(mockConnectionFactory.create()).willReturn(mockJdbConnection0);
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, longLeaseLife);

    //borrow them all but don't return
    for (int i = 0; i < poolSize; i++) {
      pool.borrow();
    }

    //borrow again before the others expire
    pool.borrow();
  }

  @Test
  public void returningAConnection() throws InterruptedException, ConnectionPoolException {
    given(mockConnectionFactory.create()).willReturn(mockJdbConnection0);
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize, shortLeaseLife);

    JdbConnection pc1 = pool.borrow();
    pool.forfeit(pc1);

    assertThat(pool.borrow(), is(mockJdbConnection0));
  }

  @Test
  public void simulateMultiplementClientsCalls()
    throws InterruptedException, ConnectionPoolException, ExecutionException {

    given(mockConnectionFactory.create())
    .willReturn(mockJdbConnection0)
    .willReturn(mockJdbConnection1)
    .willReturn(mockJdbConnection2)
    .willReturn(mockJdbConnection3)
    .willReturn(mockJdbConnection4);
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, 5, 50000);

    List<Future<Integer>> resultList = new ArrayList<>();
    ExecutorService executor = Executors.newFixedThreadPool(3000);

    Callable<Integer> task = () -> {
      try {
        JdbConnection connection = pool.borrow();
        TimeUnit.SECONDS.sleep((long) (Math.random() * 2));
        pool.forfeit(connection);
        return 1;
      } catch (Exception e) {
        if (e.getMessage() == "No connections available") {
          return 1;
        } else {
          e.printStackTrace();
        }
      }
      return 0;
    };

    for (int i = 0 ; i < 15000 ; i++) {
      Future<Integer> result = executor.submit(task);
      resultList.add(result);
    }

    int total = 0;
    for (Future<Integer> future : resultList) {
      try {
        total += future.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    executor.shutdown();

    assertThat(total, is(15000));
  }
}
