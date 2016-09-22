// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans.query;

/**
 * Created by rkolliva on 10/21/2015.
 * An interface allowing for different implementations of query execution
 */
public interface MongoQueryExecutor {

  Object executeQuery(QueryProvider queryProvider) throws MongoQueryException;
}
