package com.spakai;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;

public class ConnectionPool {

  private Deque<JdbConnection> pooled = new ArrayDeque<>();
    
  private Map<Instant,JdbConnection> borrowed = new TreeMap<>();
    
  private JdbConnectionFactory factory;
    
  private long leaseTimeInMillis = Long.MAX_VALUE;

  /**
   * Creates connection objects and pushes it into queue.
   * @param factory Used to create JDBConnection implementations.
   * @param poolSize Number of JDBConnection implementations to create.
   * @param leaseTimeInMillis How long the client can use the connection before it expires.
   */
  
  public ConnectionPool(JdbConnectionFactory factory, int poolSize, long leaseTimeInMillis) {
    for (int i = 0; i < poolSize; i++) {
      pooled.addLast(factory.create());
    }
        
    this.factory = factory;
    this.leaseTimeInMillis = leaseTimeInMillis;
  }
  
  /**
   * Get a JDBConnection object either by the ones available in the queue or recycling 
   * the first expired connection. When a connection is given to a client, it is tagged with 
   * the current time. This enables us to check the duration it has been out and recycle if
   * required.
   * @return JDBConnection This contains the actual jdbc connection object to db.
   * @throws ConnectionPoolException Throws if no available connections
   */
  
  public JdbConnection borrow() throws ConnectionPoolException {
    if (pooled.size() > 0) {
      //take from the front
      borrowed.put(Instant.now(), pooled.peek());
      return pooled.removeFirst();
    } else {
      //check for the first expired connection , close it and create a replacement
      for (Map.Entry<Instant,JdbConnection> entry : borrowed.entrySet()) {
        Instant leaseTime = entry.getKey();
        JdbConnection jdbConnection = entry.getValue();
        Duration timeElapsed = Duration.between(leaseTime, Instant.now());
        if (timeElapsed.toMillis() > leaseTimeInMillis) {
          //expired, let's close it and remove it from existence
          jdbConnection.close();
          borrowed.remove(leaseTime);

          //create a new one, mark it as borrowed and give it to the client
          JdbConnection newJdbConnection = factory.create();
          borrowed.put(Instant.now(), newJdbConnection);
          return newJdbConnection;
        }
      }
    }
    throw new ConnectionPoolException("No connections available");
  }
  
  
  /**
   * Return a JDBConnection object back to the pool. 
   * 
   * @param jdbConnection The object retrieved from the pool via borrow()
   */
    
  public void forfeit(JdbConnection jdbConnection) {
    borrowed.values().remove(jdbConnection);
    pooled.addLast(jdbConnection);
  }
}
