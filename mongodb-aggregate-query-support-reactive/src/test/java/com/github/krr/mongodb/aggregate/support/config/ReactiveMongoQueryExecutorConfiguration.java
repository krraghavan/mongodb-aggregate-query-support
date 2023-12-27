package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.api.ReactiveMongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.ReactiveMongoNativeJavaDriverQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@Configuration
public class ReactiveMongoQueryExecutorConfiguration {

  @Autowired
  private ReactiveMongoOperations mongoOperations;

  @Bean
  public ReactiveMongoQueryExecutor queryExecutor() {
    return new ReactiveMongoNativeJavaDriverQueryExecutor(mongoOperations);
  }

}
