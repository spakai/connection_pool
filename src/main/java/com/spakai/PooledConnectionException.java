package com.spakai;

import java.lang.Exception;

public class PooledConnectionException extends Exception {
  public PooledConnectionException(String message) {
    super(message);
  }

  public PooledConnectionException(Throwable cause) {
     initCause(cause);
  }

  public PooledConnectionException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
}
