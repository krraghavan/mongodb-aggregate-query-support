package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.NonReactiveMongoNativeJavaDriverQueryExecutor;
import com.mongodb.MongoClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by rkolliva
 * 4/28/18.
 */

@Configuration
@Import(NonReactiveMongoClientTestConfiguration.class)
public class NonReactiveMongoDbTestConfiguration {

  @Bean
  public String mongoDbName() {
    return RandomStringUtils.randomAlphabetic(7);
  }

  @Bean
  public MongoOperations mongoOperations(MongoClient mongoClient, String dbName) {
    return new MongoTemplate(mongoClient, dbName);
  }

  @Bean
  public MongoQueryExecutor queryExecutor(MongoOperations mongoOperations) {
    return new NonReactiveMongoNativeJavaDriverQueryExecutor(mongoOperations);
  }


}
