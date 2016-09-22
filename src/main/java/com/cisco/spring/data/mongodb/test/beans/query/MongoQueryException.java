// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans.query;

/**
 * Created by rkolliva on 10/24/2015.
 * Used for query execution failures
 */
public class MongoQueryException extends Exception {

  public MongoQueryException() {
  }

  public MongoQueryException(String message) {
    super(message);
  }

  public MongoQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public MongoQueryException(Throwable cause) {
    super(cause);
  }

  public MongoQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
