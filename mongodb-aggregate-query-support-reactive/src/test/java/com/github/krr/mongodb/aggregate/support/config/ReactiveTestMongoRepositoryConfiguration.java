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

import com.github.krr.mongodb.aggregate.support.factory.ReactiveAggregateQuerySupportingRepositoryFactoryBean;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveTestAggregateRepositoryMarker;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * Created by rkolliva
 * 10/20/2015.
 */
@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = ReactiveTestAggregateRepositoryMarker.class,
                                 repositoryFactoryBeanClass = ReactiveAggregateQuerySupportingRepositoryFactoryBean.class,
                                 reactiveMongoTemplateRef = "reactiveMongoOperations")
public class ReactiveTestMongoRepositoryConfiguration {

}
