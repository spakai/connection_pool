package com.spakai;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

import java.sql.Connection;
import java.sql.SQLException;

public class PooledConnectionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock JDBConnection mockJDBConnection;
    @Mock Connection mockConnection;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void LeaseTimeExpired() throws java.lang.InterruptedException, PooledConnectionException {
        PooledConnection pooledConn = new PooledConnection(mockJDBConnection, 1000);
        pooledConn.set();
        Thread.sleep(2000);

        assertThat(pooledConn.isExpired(), is(true));
    }

    @Test
    public void LeaseTimeStillValid() throws java.lang.InterruptedException, PooledConnectionException {
        PooledConnection pooledConn = new PooledConnection(mockJDBConnection, 5000);
        pooledConn.set();
        Thread.sleep(2000);

        assertThat(pooledConn.isExpired(), is(false));
    }

    @Test
    public void ConnectionIsStillActive() throws SQLException {
        when(mockJDBConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
 
        PooledConnection pooledConn = new PooledConnection(mockJDBConnection, 5000);

        assertThat(pooledConn.isActive(), is(true));
    }

    @Test
    public void ConnectionThrowsException() throws SQLException {
        when(mockJDBConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenThrow(new SQLException());
 
        PooledConnection pooledConn = new PooledConnection(mockJDBConnection, 5000);

        assertThat(pooledConn.isActive(), is(false));
    }


}
