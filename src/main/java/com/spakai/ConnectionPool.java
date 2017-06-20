package com.spakai;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionPool {

  private ConcurrentLinkedQueue<JdbConnection> pool = new ConcurrentLinkedQueue<>();

  private Map<JdbConnection,Instant> borrowed = new ConcurrentHashMap<>();

  private JdbConnectionFactory factory;

  private long leaseTimeInMillis;

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
   * @throws ConnectionPoolException Throws if no available connections
   */

  public JdbConnection borrow() throws ConnectionPoolException {
    if (pool.size() > 0) {
      borrowed.put(pool.peek(),Instant.now());
      return pool.remove();
    } else {
      return createReplacementIfExpiredConnFound();
    }
  }

  /**
   * Return a JdbConnection object back to the pool.
   *
   * @param jdbConnection The object retrieved from the pool via borrow()
   * @throws ConnectionPoolException Throws if connection has already been 
   *        returned or forced to expire
   */

  public void forfeit(JdbConnection jdbConnection) throws ConnectionPoolException {
    if (borrowed.containsKey(jdbConnection)) {
      borrowed.remove(jdbConnection);
      pool.add(jdbConnection);
    } else {
      throw new ConnectionPoolException("Connection already returned or forced to expire");
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
