package com.spakai;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ConnectionPool {

  private Deque<JdbConnection> pooled = new ArrayDeque<>();

  private Map<JdbConnection,Instant> borrowed = new HashMap<>();

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
      pooled.addLast(factory.create());
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

  public synchronized JdbConnection borrow() throws ConnectionPoolException {
    if (pooled.size() > 0) {
      borrowed.put(pooled.peek(),Instant.now());
      return pooled.removeFirst();
    } else {
      return createReplacementIfExpiredConnFound();
    }
  }

  /**
   * Return a JdbConnection object back to the pool.
   *
   * @param jdbConnection The object retrieved from the pool via borrow()
   * @throws ConnectionPoolException Throws if connection has already been returned or forced to expire
   */

  public synchronized void forfeit(JdbConnection jdbConnection) throws ConnectionPoolException {
    if (borrowed.containsKey(jdbConnection)) {
      borrowed.remove(jdbConnection);
      pooled.addLast(jdbConnection);
    } else {
      throw new ConnectionPoolException("Connection already returned or forced to expire");
    }
  }

  private JdbConnection createReplacementIfExpiredConnFound() throws ConnectionPoolException {
    //check for the first expired connection , close it and create a replacement
    //throw exception if replacement is not possible

    Entry<JdbConnection, Instant> entry =
    borrowed.entrySet().parallelStream()
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
