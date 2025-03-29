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


import com.github.krr.mongodb.aggregate.support.api.ReactiveMongoQueryExecutor;
import java.io.Serializable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Created by rkolliva 9/7/16.
 */
@Component
public class ReactiveAggregateQuerySupportingRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
    extends ReactiveMongoRepositoryFactoryBean<T, S, ID> implements ApplicationContextAware {

  private final ReactiveMongoQueryExecutor queryExecutor;
  private final Environment environment;
  private ApplicationContext applicationContext;

  @Autowired
  public ReactiveAggregateQuerySupportingRepositoryFactoryBean(
      Class<? extends T> repositoryInterface,
      ReactiveMongoQueryExecutor queryExecutor,
      Environment environment) {
    super(repositoryInterface);
    this.queryExecutor = queryExecutor;
    this.environment = environment;
  }

  @Override
  protected @NonNull RepositoryFactorySupport getFactoryInstance(
      @NonNull ReactiveMongoOperations operations) {
    Assert.notNull(queryExecutor, "Query executor cannot be null");
    return new ReactiveAggregateQuerySupportingRepositoryFactory(operations, queryExecutor,
        applicationContext, environment);
  }

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }
}