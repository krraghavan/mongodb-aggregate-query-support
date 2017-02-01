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
package com.cisco.mongodb.aggregate.support.test.config;

import com.cisco.mongodb.aggregate.support.query.JongoQueryExecutor;
import com.cisco.mongodb.aggregate.support.query.MongoQueryExecutor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Qualifier;
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

import java.net.UnknownHostException;

/**
 * Created by rkolliva
 * 10/21/2015.
 */
@Configuration
@Import({FongoConfiguration.class, MongoClientConfiguration.class})
public class MongoDBTestConfiguration {

  @Bean
  public MongoDbFactory mongoDbFactory(MongoClient mongoClient, String dbName) throws UnknownHostException {
    return new SimpleMongoDbFactory(mongoClient, dbName);
  }

  @Bean
  public MongoDbFactory mongoDbFactoryForJongo(MongoClient mongoClient, String dbName) throws UnknownHostException {
    return new SimpleMongoDbFactory(mongoClient, dbName);
  }

  @Bean
  public MongoMappingContext mongoMappingContext() {
    return new MongoMappingContext();
  }

  @Bean
  public DbRefResolver dbRefResolver(@Qualifier("mongoDbFactory") MongoDbFactory mongoDbFactory) {
    return new DefaultDbRefResolver(mongoDbFactory);
  }

  @Bean
  public MongoTemplate mongoTemplate(@Qualifier("mongoDbFactory") MongoDbFactory mongoDbFactory,
                                     MongoConverter mongoConverter) throws Exception {
    MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, mongoConverter);
    mongoTemplate.setWriteConcern(WriteConcern.JOURNALED);
    return mongoTemplate;
  }

  @Bean
  public MappingMongoConverter mongoConverter(MongoMappingContext mappingContext,
                                              DbRefResolver dbRefResolver) throws Exception {
    return new MappingMongoConverter(dbRefResolver, mappingContext);
  }

  @Bean
  public Jongo jongo(@Qualifier("mongoDbFactoryForJongo") MongoDbFactory mongoDbFactoryForJongo) throws UnknownHostException {
    return new Jongo(mongoDbFactoryForJongo.getDb());
  }

  @Bean
  public MongoQueryExecutor queryExecutor(Jongo jongo) throws UnknownHostException {
    return new JongoQueryExecutor(jongo);
  }
}
