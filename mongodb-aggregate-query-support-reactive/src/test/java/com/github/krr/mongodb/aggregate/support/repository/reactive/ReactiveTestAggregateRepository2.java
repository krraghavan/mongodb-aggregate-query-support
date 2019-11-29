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
package com.github.krr.mongodb.aggregate.support.repository.reactive;


import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.TestAggregateAnnotation2FieldsBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva on 10/18/2015.
 *
 */
public interface ReactiveTestAggregateRepository2 extends
                                                  ReactiveTestMongoRepository<TestAggregateAnnotation2FieldsBean, String> {


  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class)
  @Match(query = "{'randomAttribute2':?0}", order = 0)
  @Project(query = "{'randomAttribute' : '$randomAttribute1', '_id' : 0}", order = 1)
  Flux<Map<String, String>> aggregateQueryWithMatchAndProjection(int value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class)
  @Match(query = "{'randomAttribute2':?0}", order = 0)
  @Limit(query = "?1", order = 1)
  Flux<Map<String, String>> aggregateQueryWithMatchAndLimit(int value, int limit);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class)
  @Out(query = "?0")
  Mono<Void> aggregateQueryWithOut(String outputRepository, @CollectionName String coll);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class)
  @Match(query = "{'randomAttribute1':?0}", order = 0)
  @Out(query = "?2")
  Mono<Void> aggregateQueryWithMatchAndOut(String randomAttribute, @CollectionName String srcRepo, String outputRepository);
}