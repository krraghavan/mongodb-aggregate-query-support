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



package com.github.krr.mongodb.aggregate.support.config;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.ExtensionAwareEvaluationContextProvider;

/**
 * @author rkolliva.
 *
 * DocumentAnnotationTestConfiguration should be loaded before the Mongo repositories configuration as the @Document
 * collection value should be calcuated before repository interface creation
 */
@Configuration
@Import({
   DocumentAnnotationTestConfiguration.class,
   MongoClientTestConfiguration.class,
//   MongoDBTestConfiguration.class,
   TestMongoRepositoryConfiguration.class,
   MongoQueryExecutorConfiguration.class
})
public class AggregateTestConfiguration {

  @Bean
  public EvaluationContextProvider evaluationContextProvider() {
    return new ExtensionAwareEvaluationContextProvider();
  }

  @Bean
  public String dbName() {
    return RandomStringUtils.randomAlphabetic(7);
  }

  @Bean
  public MongoDbFactory mongoDbFactory(MongoClient mongo, String dbName) {
    return new SimpleMongoDbFactory(mongo, dbName);
  }

  @Bean
  public MongoMappingContext mongoMappingContext() {
    return new MongoMappingContext();
  }

  @Bean
  public DbRefResolver dbRefResolver(MongoDbFactory mongoDbFactory) {
    return new DefaultDbRefResolver(mongoDbFactory);
  }

  @Bean
  public MongoTemplate mongoTemplate(MongoClient mongo, MongoConverter mongoConverter, String dbName) {
    MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(mongo, dbName), mongoConverter);
    mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);
    return mongoTemplate;
  }

  @Bean
  public MappingMongoConverter mongoConverter(MongoMappingContext mappingContext, DbRefResolver dbRefResolver) {
    return new MappingMongoConverter(dbRefResolver, mappingContext);
  }
}
