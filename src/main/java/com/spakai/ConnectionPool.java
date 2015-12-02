package com.spakai;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ConnectionPool {

    private Deque<PooledConnection> availableConnections = new ArrayDeque<>();
    
    private Set<PooledConnection> usedConnections = new HashSet<>();
    
    private JDBConnectionFactory factory;
    
    private long leaseTimeInMillis;

    public ConnectionPool(JDBConnectionFactory factory, int poolSize, long leaseTimeInMillis) {
        for(int i=0; i < poolSize; i++) {
        	//push to the back
        	availableConnections.addLast(new PooledConnection(i,factory.create(), leaseTimeInMillis));
        }
        
        this.factory = factory;
        this.leaseTimeInMillis = leaseTimeInMillis;
        
    }
    
    public PooledConnection borrow() throws ConnectionPoolException {
    	if(availableConnections.size() > 0) {
    		//take from the front
    		PooledConnection pooledConnection = availableConnections.peek(); 
    		pooledConnection.set();
    		usedConnections.add(pooledConnection);
    		return availableConnections.remove();
    	} else {
    		//check for the first expired connection , close it and create a replacement
    		for(PooledConnection pc : usedConnections) {
    			if(pc.isExpired()) {
    				pc.closeConnection();
    				usedConnections.remove(pc);
    				PooledConnection pooledConnection = new PooledConnection(100,factory.create(), leaseTimeInMillis);
    				pooledConnection.set();
    				usedConnections.add(pooledConnection);
    				return pooledConnection;
    			}
    		}
    	}
    	
		throw new ConnectionPoolException("No connections available");
    }
    
    public void forfeit(PooledConnection pooledConnection) {
    	usedConnections.remove(pooledConnection);
    	availableConnections.addLast(pooledConnection);
    	pooledConnection.reset();
    }
    
}
