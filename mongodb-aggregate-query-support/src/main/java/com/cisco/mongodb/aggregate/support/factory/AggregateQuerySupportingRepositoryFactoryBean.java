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

package com.cisco.mongodb.aggregate.support.factory;


import com.cisco.mongodb.aggregate.support.query.MongoQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Created by rkolliva
 * 9/7/16.
 */
@Component
public class AggregateQuerySupportingRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
    extends MongoRepositoryFactoryBean<T, S, ID> {

  private final MongoQueryExecutor queryExecutor;

  @Autowired
  public AggregateQuerySupportingRepositoryFactoryBean(Class<? extends T> repositoryInterface,
                                                       MongoQueryExecutor queryExecutor) {
    super(repositoryInterface);
    this.queryExecutor = queryExecutor;
  }

  @Override
  protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
    Assert.notNull(queryExecutor);
    return new AggregateQuerySupportingRepositoryFactory(operations, queryExecutor);
  }
}