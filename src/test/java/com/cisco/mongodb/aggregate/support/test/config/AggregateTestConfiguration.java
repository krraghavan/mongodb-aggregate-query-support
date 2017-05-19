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

import com.cisco.mongodb.aggregate.support.api.ResultsExtractor;
import com.cisco.mongodb.aggregate.support.query.JongoBasedAggregateResultExtractor;
import org.jongo.Jongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
   MongoDBTestConfiguration.class,
   TestMongoRepositoryConfiguration.class
})
public class AggregateTestConfiguration {

  @Bean
  public EvaluationContextProvider evaluationContextProvider() {
    return new ExtensionAwareEvaluationContextProvider();
  }

  @Bean
  public ResultsExtractor resultsUnmarshaller(Jongo jongo) {
    return new JongoBasedAggregateResultExtractor(jongo);
  }
}
