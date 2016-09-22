package com.cisco.spring.data.mongodb.test.beans.factory;

/**
 * Created by rkolliva
 * 9/7/16.
 */
// Copyright (c) 2015-2016 by Cisco Systems, Inc.

import com.cisco.spring.data.mongodb.test.beans.query.MongoQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import java.io.Serializable;

public class AggregateQuerySupportingRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
    extends MongoRepositoryFactoryBean<T, S, ID> {

  @Autowired
  private MongoQueryExecutor queryExecutor;

  @Override
  protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
    Assert.notNull(queryExecutor);
    return new AggregateQuerySupportingRepositoryFactory(operations, queryExecutor);
  }
}