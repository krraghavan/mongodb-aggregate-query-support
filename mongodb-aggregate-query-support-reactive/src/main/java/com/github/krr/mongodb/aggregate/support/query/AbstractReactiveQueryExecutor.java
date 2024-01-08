package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.ReactiveMongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.deserializers.BsonDocumentObjectMapper;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

/**
 * Created by rkolliva
 * 5/17/18.
 */

@SuppressWarnings("WeakerAccess")
public abstract class AbstractReactiveQueryExecutor implements ReactiveMongoQueryExecutor {

  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new BsonDocumentObjectMapper();

  protected final ObjectMapper objectMapper;

  protected final ReactiveMongoOperations mongoOperations;

  protected AbstractReactiveQueryExecutor(ReactiveMongoOperations mongoOperations, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.mongoOperations = mongoOperations;
  }

  public AbstractReactiveQueryExecutor(ReactiveMongoOperations mongoOperations) {
    this(mongoOperations, DEFAULT_OBJECT_MAPPER);
  }
}
