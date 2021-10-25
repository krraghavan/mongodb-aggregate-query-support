package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.NonReactiveMongoNativeJavaDriverQueryExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@Configuration
public class NonReactiveMongoQueryExecutorConfiguration {

  @Bean
  public MongoQueryExecutor queryExecutor(MongoOperations nonReactiveMongoOperations) {
    return new NonReactiveMongoNativeJavaDriverQueryExecutor(nonReactiveMongoOperations);
  }

}
