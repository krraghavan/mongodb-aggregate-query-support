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
package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Match;
import com.github.krr.mongodb.aggregate.support.beans.TestAggregateAnnotationBean;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Mono;

/**
 * Created by rkolliva
 * 10/10/2015.
 */
public interface ReactiveTestAggregateRepository extends
                                                 ReactiveTestMongoRepository<TestAggregateAnnotationBean, String> {

  @Query("{'randomAttribute': ?0}")
  TestAggregateAnnotationBean mongoQueryAnnotation(String value);

  @Aggregate(inputType = TestAggregateAnnotationBean.class, outputBeanType = TestAggregateAnnotationBean.class)
  @Match(query = "{'randomAttribute':?0}", order = 0)
  Mono<TestAggregateAnnotationBean> aggregateQueryMatchAnnotation(String value);

}
