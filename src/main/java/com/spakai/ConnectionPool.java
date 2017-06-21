package com.spakai;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {

  private final ConcurrentLinkedQueue<JdbConnection> pool = new ConcurrentLinkedQueue<>();

  private final Map<JdbConnection,Instant> borrowed = new ConcurrentHashMap<>();

  private final JdbConnectionFactory factory;

  private final long leaseTimeInMillis;

  /**
   * Creates connection objects and pushes it into queue.
   * @param factory Used to create JDBConnection implementations.
   * @param poolSize Number of JDBConnection implementations to create.
   * @param leaseTimeInMillis How long the client can use the connection before it expires.
   */

  public ConnectionPool(JdbConnectionFactory factory, int poolSize, long leaseTimeInMillis) {
    for (int i = 0; i < poolSize; i++) {
      pool.add(factory.create());
    }

    this.factory = factory;
    this.leaseTimeInMillis = leaseTimeInMillis;
  }

  /**
   * Get a JdbConnection object either by the ones available in the queue or replace
   * the first expired connection. When a connection is given to a client, it is tagged with
   * the current time. This enables us to check the duration it has been out and replace if
   * required.
   * @return JDBConnection This contains the actual jdbc connection object to db.
   */

  public JdbConnection borrow() {
     
    JdbConnection conn = pool.poll();
    if ( conn != null) {
      borrowed.put(conn,Instant.now());  
      return conn;
    }    
    
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

  private JdbConnection createReplacementIfExpiredConnFound() throws ConnectionPoolException {
    //check for the first expired connection , close it and create a replacement
    //throw exception if replacement is not possible

    Entry<JdbConnection, Instant> entry =
        borrowed.entrySet().stream()
                        .filter(e -> hasExpired(e.getValue()))
                        .findFirst()
                        .orElseThrow(() -> new ConnectionPoolException("No connections available"));
    
    entry.getKey().close();
    borrowed.remove(entry.getKey());
    JdbConnection newJdbConnection = factory.create();
    borrowed.put(newJdbConnection,Instant.now());
    return newJdbConnection;
  }
  
  private boolean hasExpired(Instant instant) {
    return (Duration.between(instant, Instant.now()).toMillis() > leaseTimeInMillis);
  }
}
