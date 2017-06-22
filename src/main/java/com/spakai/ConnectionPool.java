package com.spakai;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionPool {

  private final ConcurrentLinkedQueue<JdbConnection> pool = new ConcurrentLinkedQueue<>();

  private final Map<JdbConnection,Instant> borrowed = new ConcurrentHashMap<>();

  private final JdbConnectionFactory factory;

  /**
   * Creates connection objects and pushes it into queue.
   * @param factory Used to create JDBConnection implementations.
   * @param poolSize Number of JDBConnection implementations to create.
   */

  public ConnectionPool(JdbConnectionFactory factory, int poolSize) {
    for (int i = 0; i < poolSize; i++) {
      pool.add(factory.create());
    }

    this.factory = factory;
    
  }

  /**
   * Get a JdbConnection object available in the queue.
   * When a connection is given to a client, it is tagged with
   * the current time. This enables us to check the duration it has been out.
   * @return JDBConnection or null This contains the actual jdbc connection object to db.
   */

  public JdbConnection borrow(final long timeoutInMillis) {
    
    final Instant startInstant = Instant.now();
    
    do {
      JdbConnection conn = pool.poll();
      if (conn != null) {
        borrowed.put(conn,Instant.now());  
        return conn;
      }    
    } while (Duration.between(startInstant, Instant.now()).toMillis() <  timeoutInMillis);
    
    return null;
  }

  /**
   * Return a JdbConnection object that was previously borrowed back to the pool.
   *
   * @param jdbConnection The object retrieved from the pool via borrow()
   */

  public void forfeit(JdbConnection jdbConnection) {
    if (borrowed.containsKey(jdbConnection)) {
      borrowed.remove(jdbConnection);
      pool.add(jdbConnection);
    } 
  }
}
