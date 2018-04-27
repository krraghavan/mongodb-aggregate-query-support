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
import com.github.krr.mongodb.aggregate.support.query.ReactiveMongoNativeJavaDriverQueryExecutor;
import com.mongodb.reactivestreams.client.MongoClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

/**
 * Created by rkolliva
 * 10/21/2015.
 */
@Configuration
@Import(ReactiveMongoClientTestConfiguration.class)
public class ReactiveMongoDbTestConfiguration {

  @Bean
  public String mongoDbName() {
    return RandomStringUtils.randomAlphabetic(7);
  }

  @Bean
  public ReactiveMongoOperations reactiveMongoOperations(MongoClient mongoClient, String dbName) {
    return new ReactiveMongoTemplate(mongoClient, dbName);
  }

  @Bean
  public MongoQueryExecutor queryExecutor(ReactiveMongoOperations mongoOperations) {
    return new ReactiveMongoNativeJavaDriverQueryExecutor(mongoOperations);
  }

}
