package com.github.krr.mongodb.aggregate.support.api;

import com.github.krr.mongodb.aggregate.support.exceptions.MongoQueryException;

/**
 * Created by krraghavan on 10/23/2021
 * An interface allowing for different implementations of reactive return types
 */
public interface ReactiveMongoQueryExecutor {

  @SuppressWarnings("rawtypes")
  Object executeQuery(QueryProvider queryProvider) throws MongoQueryException;
}

