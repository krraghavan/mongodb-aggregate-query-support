package com.cisco.spring.data.mongodb.test.beans.factory;

/**
 * Created by rkolliva
 * 9/7/16.
 */
// Copyright (c) 2015-2016 by Cisco Systems, Inc.

import com.cisco.spring.data.mongodb.test.beans.annotation.Aggregate;
import com.cisco.spring.data.mongodb.test.beans.query.AggregateMongoQuery;
import com.cisco.spring.data.mongodb.test.beans.query.MongoQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by rkolliva
 * 10/10/2015.
 */
public class AggregateQuerySupportingRepositoryFactory extends MongoRepositoryFactory {

  private final MongoOperations mongoOperations;

  @Autowired
  private MongoQueryExecutor queryExecutor;

  /**
   * Creates a new {@link MongoRepositoryFactory} with the given {@link MongoOperations}.
   *
   * @param mongoOperations must not be {@literal null}
   * @param queryExecutor
   */
  public AggregateQuerySupportingRepositoryFactory(MongoOperations mongoOperations, MongoQueryExecutor queryExecutor) {
    super(mongoOperations);
    this.mongoOperations = mongoOperations;
    this.queryExecutor = queryExecutor;
  }

  @Override
  protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key,
                                                       EvaluationContextProvider evaluationContextProvider) {

    QueryLookupStrategy parentQueryLookupStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
    return new AggregateQueryLookupStrategy(parentQueryLookupStrategy);
  }

  public void setQueryExecutor(MongoQueryExecutor queryExecutor) {
    this.queryExecutor = queryExecutor;
  }

  private class AggregateQueryLookupStrategy implements QueryLookupStrategy {

    private final QueryLookupStrategy parentQueryLookupStrategy;

    public AggregateQueryLookupStrategy(QueryLookupStrategy parentQueryLookupStrategy) {
      this.parentQueryLookupStrategy = parentQueryLookupStrategy;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {
      Annotation aggregateAnnotation = method.getAnnotation(Aggregate.class);
      if (aggregateAnnotation == null) {
        return parentQueryLookupStrategy.resolveQuery(method, metadata, namedQueries);
      }
      else {
        return new AggregateMongoQuery(method, metadata, mongoOperations, queryExecutor);
      }
    }
  }
}