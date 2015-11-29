package com.spakai;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;

import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

public class PooledConnectionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock JDBConnection mockConnection;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void LeaseTimeExpired() throws java.lang.InterruptedException, PooledConnectionException {
        PooledConnection pooledConn = new PooledConnection(mockConnection, 1000);
        pooledConn.getConnection();
        Thread.sleep(2000);

        assertThat(pooledConn.isExpired(), is(true));
    }

    @Test
    public void LeaseTimeStillValid() throws java.lang.InterruptedException, PooledConnectionException {
        PooledConnection pooledConn = new PooledConnection(mockConnection, 5000);
        pooledConn.getConnection();
        Thread.sleep(2000);

        assertThat(pooledConn.isExpired(), is(false));
    }

    @Test
    public void GetConnectionsTwice() throws PooledConnectionException {

        thrown.expect(PooledConnectionException.class);
        thrown.expectMessage("Connection already in use");

        PooledConnection pooledConn = new PooledConnection(mockConnection, 5000);
        pooledConn.getConnection();
        pooledConn.getConnection();


    }
}
