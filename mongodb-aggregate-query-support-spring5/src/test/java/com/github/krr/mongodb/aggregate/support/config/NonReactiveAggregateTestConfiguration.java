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

import com.github.krr.mongodb.embeddedmongo.config.MongoTestServerConfiguration;
import com.mongodb.client.MongoClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.repository.query.ExtensionAwareQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

/**
 * @author rkolliva.
 *
 * DocumentAnnotationTestConfiguration should be loaded before the Mongo repositories configuration as the @Document
 * collection value should be calcuated before repository interface creation
 */
@Configuration
@Import({
            DocumentAnnotationTestConfiguration.class,
            MongoTestServerConfiguration.class,
            NonReactiveMongoClientTestConfiguration.class,
            NonReactiveTestMongoRepositoryConfiguration.class,
            NonReactiveMongoQueryExecutorConfiguration.class
})
public class NonReactiveAggregateTestConfiguration {


  @Bean
  public String mongoDbName() {
    return RandomStringUtils.randomAlphabetic(7);
  }

  @Bean
  public MongoOperations nonReactiveMongoOperations(MongoClient mongoClient, String dbName) {
    return new MongoTemplate(mongoClient, dbName);
  }

  @Bean
  public QueryMethodEvaluationContextProvider evaluationContextProvider(ApplicationContext applicationContext) {
    return new ExtensionAwareQueryMethodEvaluationContextProvider(applicationContext);
  }

}
