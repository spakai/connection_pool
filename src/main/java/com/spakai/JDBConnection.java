package com.spakai;

import java.sql.Connection;

public interface JDBConnection {
  public Connection getConnection();
  public void close();
}