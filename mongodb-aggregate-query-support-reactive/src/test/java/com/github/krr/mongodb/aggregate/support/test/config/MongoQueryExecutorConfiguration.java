package com.github.krr.mongodb.aggregate.support.test.config;

import com.github.krr.mongodb.aggregate.support.query.MongoNativeReactiveJavaDriverQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.MongoQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class MongoQueryExecutorConfiguration {

  @Autowired
  private ReactiveMongoOperations mongoOperations;

  @Bean
  public MongoQueryExecutor queryExecutor() {
    return new MongoNativeReactiveJavaDriverQueryExecutor(mongoOperations);
  }

//  @Bean
//  public ReactiveMongoOperations reactiveMongoOperations(MongoClient mongoClient, String dbName) {
//    return new ReactiveMongoTemplate(mongoClient, dbName);
//  }

}
