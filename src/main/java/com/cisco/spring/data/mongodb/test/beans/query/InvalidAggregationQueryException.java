// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans.query;

/**
 * Created by rkolliva on 10/11/2015.
 */
public class InvalidAggregationQueryException extends Exception {

  public InvalidAggregationQueryException() {
  }

  public InvalidAggregationQueryException(String message) {
    super(message);
  }

  public InvalidAggregationQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidAggregationQueryException(Throwable cause) {
    super(cause);
  }

  public InvalidAggregationQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
