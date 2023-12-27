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
package com.github.krr.mongodb.aggregate.support.query;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.api.ReactiveMongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.exceptions.InvalidAggregationQueryException;
import com.github.krr.mongodb.aggregate.support.exceptions.MongoQueryException;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by rkolliva
 * 4/16/2018
 */
@SuppressWarnings("NullableProblems")
@Component
public class ReactiveAggregateMongoQuery extends AbstractReactiveMongoQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveAggregateMongoQuery.class);

  private final ReactiveMongoOperations mongoOperations;

  private final Method method;

  private final ReactiveMongoQueryExecutor queryExecutor;

  private static final ReactiveQueryMethodEvaluationContextProvider CONTEXT_PROVIDER =
      new ApplicationContextQueryMethodEvaluationContextProvider();

  /**
   * Creates a new {@link AbstractMongoQuery} from the given {@link MongoQueryMethod} and {@link MongoOperations}.
   *
   * @param method must not be {@literal null}.
   * @param projectionFactory - the projection factory
   */
  @Autowired
  public ReactiveAggregateMongoQuery(Method method, RepositoryMetadata metadata, ReactiveMongoOperations mongoOperations,
                                     ProjectionFactory projectionFactory, ReactiveMongoQueryExecutor queryExecutor) {
    super(new ReactiveMongoQueryMethod(method, metadata, projectionFactory,
                                       mongoOperations.getConverter().getMappingContext()),
          mongoOperations,
          new SpelExpressionParser(),
          CONTEXT_PROVIDER);
    this.mongoOperations = mongoOperations;
    this.method = method;
    this.queryExecutor = queryExecutor;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Publisher<Object> execute(Object[] parameters) {
    MongoParameterAccessor mongoParameterAccessor = new MongoParametersParameterAccessor(getQueryMethod(), parameters);
    ConvertingParameterAccessor parameterAccessor = new ConvertingParameterAccessor(mongoOperations.getConverter(),
                                                                                    mongoParameterAccessor);
    try {
      AbstractAggregateQueryProvider aggregateQueryProvider = createAggregateQueryProvider(mongoParameterAccessor,
                                                                                           parameterAccessor);
      return (Publisher<Object>) queryExecutor.executeQuery(aggregateQueryProvider);
    }
    catch (MongoQueryException e) {
      LOGGER.error("Error executing aggregate query", e);
      throw new IllegalStateException(e);
    }
    catch (InvalidAggregationQueryException e) {
      LOGGER.error("Invalid aggregation query", e);
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Factory method for creating the proper Query provider
   *
   * @param mongoParameterAccessor - mongo parameter accessor
   * @param parameterAccessor      - converting parameter accessor
   * @return - the query provider
   * @throws InvalidAggregationQueryException - if there was an error creating the query provider
   */
  private AbstractAggregateQueryProvider createAggregateQueryProvider(MongoParameterAccessor mongoParameterAccessor,
                                                                      ConvertingParameterAccessor parameterAccessor)
      throws InvalidAggregationQueryException {

    Annotation annotation = method.getAnnotation(Aggregate.class);
    Assert.notNull(annotation, "Aggregate2 must be specified on the method");
    return new ReactiveAggregateQueryProvider(method, mongoParameterAccessor, parameterAccessor);
  }

  @Override
  protected Mono<Query> createQuery(ConvertingParameterAccessor accessor) {
    throw new UnsupportedOperationException("AggregateMongoQuery does not support createQuery");
  }

  @Override
  protected boolean isCountQuery() {
    return false;
  }

  @Override
  protected boolean isExistsQuery() {
    return false;
  }

  @Override
  protected boolean isDeleteQuery() {
    return false;
  }

  @Override
  protected boolean isLimiting() {
    // @todo change this to see if Pageable is specified or if limit stage exists in pipeline.
    return false;
  }

}
