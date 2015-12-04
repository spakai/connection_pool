package com.spakai;

import java.sql.Connection;

public interface JdbConnection {
  public Connection getConnection();
  
  public void close();
}
