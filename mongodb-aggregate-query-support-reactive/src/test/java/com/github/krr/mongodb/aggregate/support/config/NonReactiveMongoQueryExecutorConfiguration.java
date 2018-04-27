package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.NonReactiveMongoNativeJavaDriverQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class NonReactiveMongoQueryExecutorConfiguration {

  @Autowired
  private MongoOperations mongoOperations;

  @Bean
  public MongoQueryExecutor queryExecutor() {
    return new NonReactiveMongoNativeJavaDriverQueryExecutor(mongoOperations);
  }

}
