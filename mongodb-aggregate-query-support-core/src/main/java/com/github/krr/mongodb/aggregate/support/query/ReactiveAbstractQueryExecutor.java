package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.ReactiveMongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.deserializers.BsonDocumentObjectMapper;

public abstract class ReactiveAbstractQueryExecutor<T> implements ReactiveMongoQueryExecutor {

  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new BsonDocumentObjectMapper();

  protected final ObjectMapper objectMapper;

  protected final T mongoOperations;

  protected ReactiveAbstractQueryExecutor(T mongoOperations, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.mongoOperations = mongoOperations;
  }

  public ReactiveAbstractQueryExecutor(T mongoOperations) {
    this(mongoOperations, DEFAULT_OBJECT_MAPPER);
  }
}

