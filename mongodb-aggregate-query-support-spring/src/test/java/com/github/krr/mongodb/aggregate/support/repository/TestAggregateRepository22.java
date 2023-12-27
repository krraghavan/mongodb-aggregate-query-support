/*
 *  Copyright (c) 2017 the original author or authors.
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


import com.github.krr.mongodb.aggregate.support.DummyAnnotation;
import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.TestAggregateAnnotation2FieldsBean;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva on 4/172015.
 *
 */
public interface TestAggregateRepository22 extends TestMongoRepository<TestAggregateAnnotation2FieldsBean, String> {

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute2':?0}", order = 0)
  @Project(query = "{'randomAttribute' : '$randomAttribute1', '_id' : 0}", order = 1)
  List<Map<String, String>> aggregateQueryWithMatchAndProjection(int value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute2':?0}", order = 0)
  @Limit(query = "?1", order = 1)
  List<Map<String, String>> aggregateQueryWithMatchAndLimit(int value, int limit);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = TestAggregateAnnotation2FieldsBean.class)
  @Out(query = "?0")
  void aggregateQueryWithOut(String outputRepository);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = TestAggregateAnnotation2FieldsBean.class)
  @Match(query = "{'randomAttribute1':?0}", order = 0)
  @Out(query = "?1")
  void aggregateQueryWithMatchAndOut(String randomAttribute, String outputRepository);

  // THESE METHODS TEST DIFFERENT ANNOTATION COMBINATIONS
  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
  @Match(query = "dont care", order = 0)
  void aggregateQueryWithMatchOnly();

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
  @Match(query = "dont care", order = 0)
  @Match(query = "dont care1", order = 1)
  void aggregateQueryWithMultipleMatchQueries();

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
  @Match(query = "dont care", order = 0)
  @Group(query = "dont care", order = 1)
  @Match(query = "dont care1", order = 2)
  void aggregateQueryWithMultipleMatchQueriesInNonContiguousOrder();

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
  @Match(query = "dont care", order = 0)
  @Group(query = "dont care", order = 1)
  @DummyAnnotation
  @Match(query = "dont care1", order = 2)
  void aggregateQueryWithMultipleMatchQueriesInNonContiguousOrderWithNonAggAnnotations();
}