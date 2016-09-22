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
package com.cisco.spring.data.mongodb.repository.test.aggregate;

import com.cisco.spring.data.mongodb.test.beans.TestAggregateAnnotationBean;
import com.cisco.spring.data.mongodb.test.beans.annotation.Aggregate;
import com.cisco.spring.data.mongodb.test.beans.annotation.Match;
import org.springframework.data.mongodb.repository.Query;

/**
 * Created by rkolliva
 * 10/10/2015.
 */
public interface TestAggregateRepository extends TestMongoRepository<TestAggregateAnnotationBean, String> {

  @Query("{'randomAttribute': ?0}")
  TestAggregateAnnotationBean mongoQueryAnnotation(String value);

  @Aggregate(inputType = TestAggregateAnnotationBean.class,
      outputBeanType = TestAggregateAnnotationBean.class,
      resultKey = "_id",
      match = {@Match(query = "{'randomAttribute':?0}", order = 0)}
  )
  TestAggregateAnnotationBean aggregateQueryMatchAnnotation(String value);

}
