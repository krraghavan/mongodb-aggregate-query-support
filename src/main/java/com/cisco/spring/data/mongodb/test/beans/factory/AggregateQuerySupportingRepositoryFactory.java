/*
 *  Copyright (c) 2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 */

package com.cisco.spring.data.mongodb.test.beans.factory;


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