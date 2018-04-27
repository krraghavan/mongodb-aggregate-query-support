package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.query.MongoNativeJavaDriverQueryExecutor;
import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Created by rkolliva
 * 4/25/18.
 */

@Configuration
public class MongoQueryExecutorConfiguration {

  @Bean
  public MongoQueryExecutor mongoQueryExecutor(MongoOperations mongoTemplate) {
    return new MongoNativeJavaDriverQueryExecutor(mongoTemplate);
  }

}
