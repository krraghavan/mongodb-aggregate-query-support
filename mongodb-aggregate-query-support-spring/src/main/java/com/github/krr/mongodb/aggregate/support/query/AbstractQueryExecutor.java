package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.deserializers.BsonDocumentObjectMapper;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Created by rkolliva
 * 5/17/18.
 */

@SuppressWarnings("WeakerAccess")
public abstract class AbstractQueryExecutor implements MongoQueryExecutor {

  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new BsonDocumentObjectMapper();

  protected final ObjectMapper objectMapper;

  protected final MongoOperations mongoOperations;

  protected AbstractQueryExecutor(MongoOperations mongoOperations, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.mongoOperations = mongoOperations;
  }

  public AbstractQueryExecutor(MongoOperations mongoOperations) {
    this(mongoOperations, DEFAULT_OBJECT_MAPPER);
  }
}
