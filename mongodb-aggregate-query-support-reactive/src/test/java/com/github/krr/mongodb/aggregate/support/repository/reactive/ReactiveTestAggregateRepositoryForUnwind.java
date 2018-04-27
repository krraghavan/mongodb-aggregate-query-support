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


import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Unwind;
import com.github.krr.mongodb.aggregate.support.beans.TestUnwindAggregateAnnotationBean;
import org.bson.Document;
import reactor.core.publisher.Flux;

/**
 * Created by rkolliva
 * 10/19/2015.
 */
public interface ReactiveTestAggregateRepositoryForUnwind extends
                                                          ReactiveTestMongoRepository<TestUnwindAggregateAnnotationBean, String> {

  @Aggregate(inputType = TestUnwindAggregateAnnotationBean.class)
  @Unwind(query = "'$randomListOfStrings'", order = 0)
  Flux<Document> aggregateQueryWithOnlyUnwind(int intValue1);
}
