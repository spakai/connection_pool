package com.spakai;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class ConnectionPoolTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @Mock private JdbConnection mockJdbConnection0;
  @Mock private JdbConnection mockJdbConnection1;
  @Mock private JdbConnection mockJdbConnection2;
  @Mock private JdbConnection mockJdbConnection3;
  @Mock private JdbConnection mockJdbConnection4;
  
  @Mock private JdbConnectionFactory mockConnectionFactory;


  private final int poolSize        = 5;
    
  private ScheduledExecutorService scheduledExecutorService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void factoryCreatesConnectionsOnCreation() {
    given(mockConnectionFactory.create()).willReturn(mockJdbConnection0);
    new ConnectionPool(mockConnectionFactory, poolSize);
    then(mockConnectionFactory).should(times(poolSize)).create();
  }

  @Test
  public void borrowingReturnsNullAfterTimeout() 
    throws InterruptedException, ConnectionPoolException {
	  given(mockConnectionFactory.create())
	    .willReturn(mockJdbConnection0);
	  
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, 1);
    pool.borrow(5000L);
    assertThat(pool.borrow(0L), is(nullValue()));
  }
  
  @Test
  public void borrowingReturnsAConnectionDuringRetry() 
    throws InterruptedException, ConnectionPoolException {
	  given(mockConnectionFactory.create())
	    .willReturn(mockJdbConnection0);
	  
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, 1);
    pool.borrow(10000L);
    assertThat(pool.borrow(0L), is(nullValue()));
  }
  
  @Test
  public void returningAConnection() throws InterruptedException, ConnectionPoolException {
    given(mockConnectionFactory.create()).willReturn(mockJdbConnection0);
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, poolSize);

    JdbConnection pc1 = pool.borrow(0L);
    pool.forfeit(pc1);

    assertThat(pool.borrow(0L), is(mockJdbConnection0));
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
    ConnectionPool pool = new ConnectionPool(mockConnectionFactory, 5);

    List<Future<Integer>> resultList = new ArrayList<>();
    ExecutorService executor = Executors.newFixedThreadPool(3000);

    Callable<Integer> task = () -> {
      try {
        JdbConnection connection = pool.borrow(0L);
        TimeUnit.SECONDS.sleep((long) (Math.random() * 2));
        if( connection != null) {
            pool.forfeit(connection);
        }
        return 1;
      } catch (Exception e) {
          e.printStackTrace();
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
