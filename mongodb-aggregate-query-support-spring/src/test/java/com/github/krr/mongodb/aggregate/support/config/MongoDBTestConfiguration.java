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

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.query.ApplicationContextQueryMethodEvaluationContextProvider;
import com.github.krr.mongodb.aggregate.support.query.MongoNativeJavaDriverQueryExecutor;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkolliva
 * 10/21/2015.
 */
@Configuration
@Import({
    DocumentAnnotationTestConfiguration.class,
    EmbeddedMongoConfiguration.class,
    TestMongoRepositoryConfiguration.class,
})
public class MongoDBTestConfiguration {

  @Bean
  public String dbName() {
    return RandomStringUtils.randomAlphabetic(7);
  }

  @Bean
  public MongoDatabaseFactory mongoDbFactory(MongoClient mongo, String dbName) {
    return new SimpleMongoClientDatabaseFactory(mongo, dbName);
  }

  @Bean
  public MongoMappingContext mongoMappingContext() {
    return new MongoMappingContext();
  }

  @Bean
  public DbRefResolver dbRefResolver(MongoDatabaseFactory mongoDbFactory) {
    return new DefaultDbRefResolver(mongoDbFactory);
  }

  @Bean
  public QueryMethodEvaluationContextProvider evaluationContextProvider() {
    return new ApplicationContextQueryMethodEvaluationContextProvider();
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

  @Bean
  public MongoClient mongo(int mongoDbPort) {
    ServerAddress serverAddress = new ServerAddress(ServerAddress.defaultHost(), mongoDbPort);
    List<ServerAddress> serverAddresses = new ArrayList<>();
    serverAddresses.add(serverAddress);
    MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                                                                 .applyToClusterSettings((b) -> {
                                                                   b.hosts(serverAddresses).build();
                                                                 }).build();
    return MongoClients.create(mongoClientSettings);
  }

  @Bean
  public MongoQueryExecutor mongoQueryExecutor(MongoOperations mongoTemplate) {
    return new MongoNativeJavaDriverQueryExecutor(mongoTemplate);
  }
}
