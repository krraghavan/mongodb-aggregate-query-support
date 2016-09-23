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
package com.cisco.mongodb.aggregate.support.test.repository;


import com.cisco.mongodb.aggregate.support.annotation.*;
import com.cisco.mongodb.aggregate.support.test.beans.TestAggregateAnnotation2FieldsBean;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva on 10/18/2015.
 */
public interface TestAggregateRepository2 extends TestMongoRepository<TestAggregateAnnotation2FieldsBean, String> {


  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class,
      genericType = true,
      outputBeanType = Map.class,
      match = {@Match(query = "{'randomAttribute2':?0}", order = 0)},
      project = {@Project(query = "{'randomAttribute' : '$randomAttribute1', '_id' : 0}", order = 1)})
  List<Map<String, String>> aggregateQueryWithMatchAndProjection(int value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class,
      genericType = true,
      outputBeanType = Map.class,
      match = {@Match(query = "{'randomAttribute2':?0}", order = 0)},
      limit = {@Limit(query = "?1", order = 1)})
  List<Map<String, String>> aggregateQueryWithMatchAndLimit(int value, int limit);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class,
      outputBeanType = TestAggregateAnnotation2FieldsBean.class,
      out = @Out(query = "?0"))
  void aggregateQueryWithOut(String outputRepository);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class,
      outputBeanType = TestAggregateAnnotation2FieldsBean.class,
      match = {@Match(query = "{'randomAttribute1':?0}", order = 0)},
      out = @Out(query = "?1"))
  void aggregateQueryWithMatchAndOut(String randomAttribute, String outputRepository);
}