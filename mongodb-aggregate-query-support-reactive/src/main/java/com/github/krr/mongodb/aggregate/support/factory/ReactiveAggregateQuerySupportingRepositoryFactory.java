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

package com.github.krr.mongodb.aggregate.support.factory;


import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.api.ReactiveMongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.ReactiveAggregateMongoQuery;
import java.lang.reflect.Method;
import java.util.Optional;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ValueExpressionDelegate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Created by rkolliva 10/10/2015.
 */
@SuppressWarnings("NullableProblems")
public class ReactiveAggregateQuerySupportingRepositoryFactory extends
    ReactiveMongoRepositoryFactory {

  private final ReactiveMongoOperations mongoOperations;

  private final ReactiveMongoQueryExecutor queryExecutor;
  private final ApplicationContext applicationContext;
  private final Environment environment;

  /**
   * Creates a new {@link MongoRepositoryFactory} with the given {@link MongoOperations}.
   *
   * @param mongoOperations must not be {@literal null}
   * @param queryExecutor   - the query executor
   */
  public ReactiveAggregateQuerySupportingRepositoryFactory(ReactiveMongoOperations mongoOperations,
      ReactiveMongoQueryExecutor queryExecutor,
      ApplicationContext applicationContext,
      Environment environment) {
    super(mongoOperations);
    this.mongoOperations = mongoOperations;
    this.queryExecutor = queryExecutor;
    this.applicationContext = applicationContext;
    this.environment = environment;
  }

  @Override
  public Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
      @NonNull ValueExpressionDelegate valueExpressionDelegate) {

    Optional<QueryLookupStrategy> parentQueryLookupStrategy = super.getQueryLookupStrategy(key,
        valueExpressionDelegate);
    Assert.isTrue(parentQueryLookupStrategy.isPresent(),
        "Expecting parent query lookup strategy to be present");
    return Optional.of(new AggregateQueryLookupStrategy(parentQueryLookupStrategy.get()));
  }

  private boolean isAggregateQueryAnnotated(Method method) {
    return (method.getAnnotation(Aggregate.class) != null);
  }

  @SuppressWarnings("NullableProblems")
  private class AggregateQueryLookupStrategy implements QueryLookupStrategy {

    private final QueryLookupStrategy parentQueryLookupStrategy;

    AggregateQueryLookupStrategy(QueryLookupStrategy parentQueryLookupStrategy) {
      this.parentQueryLookupStrategy = parentQueryLookupStrategy;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata repositoryMetadata,
        ProjectionFactory projectionFactory, NamedQueries namedQueries) {
      if (!isAggregateQueryAnnotated(method)) {
        return parentQueryLookupStrategy.resolveQuery(method, repositoryMetadata, projectionFactory,
            namedQueries);
      } else {
        return new ReactiveAggregateMongoQuery(method, repositoryMetadata, mongoOperations,
            projectionFactory, queryExecutor, applicationContext, environment);
      }
    }
  }
}