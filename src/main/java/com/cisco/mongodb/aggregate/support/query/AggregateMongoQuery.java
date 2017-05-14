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
package com.cisco.mongodb.aggregate.support.query;

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.annotation.v2.Aggregate2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by rkolliva
 * 10/10/2015.
 */
@Component
public class AggregateMongoQuery extends AbstractMongoQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMongoQuery.class);

  private final MongoOperations mongoOperations;

  private final Method method;

  private final MongoQueryExecutor queryExecutor;

  /**
   * Creates a new {@link AbstractMongoQuery} from the given {@link MongoQueryMethod} and {@link MongoOperations}.
   *
   * @param method must not be {@literal null}.
   * @param projectionFactory - the projection factory
   */
  @Autowired
  public AggregateMongoQuery(Method method, RepositoryMetadata metadata, MongoOperations mongoOperations,
                             ProjectionFactory projectionFactory, MongoQueryExecutor queryExecutor) {
    super(new MongoQueryMethod(method, metadata, projectionFactory, mongoOperations.getConverter().getMappingContext()),
          mongoOperations);
    this.mongoOperations = mongoOperations;
    this.method = method;
    this.queryExecutor = queryExecutor;
  }

  @Override
  public Object execute(Object[] parameters) {
    MongoParameterAccessor mongoParameterAccessor = new MongoParametersParameterAccessor(getQueryMethod(), parameters);
    ConvertingParameterAccessor parameterAccessor = new ConvertingParameterAccessor(mongoOperations.getConverter(),
                                                                                    mongoParameterAccessor);
    try {
      AbstractAggregateQueryProvider aggregateQueryProvider = createAggregateQueryProvider(mongoParameterAccessor,
                                                                                           parameterAccessor);
      return queryExecutor.executeQuery(aggregateQueryProvider, parameters);
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
    if (annotation != null) {
      return new AggregateQueryProvider(method, mongoParameterAccessor, parameterAccessor);
    }
    annotation = method.getAnnotation(Aggregate2.class);
    Assert.notNull(annotation, "Either Aggregate or Aggregate2 must be specified on the method");
    return new AggregateQueryProvider2(method, mongoParameterAccessor, parameterAccessor);
  }

  @Override
  protected Query createQuery(ConvertingParameterAccessor accessor) {
    throw new UnsupportedOperationException("AggregateMongoQuery does not support createQuery");
  }

  @Override
  protected boolean isCountQuery() {
    return false;
  }

  @Override
  protected boolean isDeleteQuery() {
    return false;
  }

}
