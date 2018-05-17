package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.deserializers.BsonDocumentObjectMapper;

/**
 * Created by rkolliva
 * 5/17/18.
 */

@SuppressWarnings("WeakerAccess")
public abstract class AbstractQueryExecutor<T> implements MongoQueryExecutor {

  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new BsonDocumentObjectMapper();

  protected final ObjectMapper objectMapper;

  protected final T mongoOperations;

  protected AbstractQueryExecutor(T mongoOperations, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.mongoOperations = mongoOperations;
  }

  public AbstractQueryExecutor(T mongoOperations) {
    this(mongoOperations, DEFAULT_OBJECT_MAPPER);
  }
}
