package com.spakai;

import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    private List<PooledConnection> connections = new ArrayList<PooledConnection>();

    public ConnectionPool(JDBConnectionFactory factory, int poolSize, long leaseTimeInMillis) {
        for(int i=0; i < poolSize; i++) {
            connections.add(new PooledConnection(factory.create(), leaseTimeInMillis));
        }
    }

    public JDBConnection borrow() {
        return null;    
    }

}
