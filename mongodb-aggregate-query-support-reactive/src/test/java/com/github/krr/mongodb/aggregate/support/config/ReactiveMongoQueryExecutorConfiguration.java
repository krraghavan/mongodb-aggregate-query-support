package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.ReactiveMongoNativeJavaDriverQueryExecutor;
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

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class ReactiveMongoQueryExecutorConfiguration {

  @Autowired
  private ReactiveMongoOperations mongoOperations;

  @Bean
  public MongoQueryExecutor queryExecutor() {
    return new ReactiveMongoNativeJavaDriverQueryExecutor(mongoOperations);
  }

}
