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

package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.v2.Aggregate2;
import com.cisco.mongodb.aggregate.support.annotation.v2.Match2;
import com.cisco.mongodb.aggregate.support.annotation.v2.Project2;
import com.cisco.mongodb.aggregate.support.test.beans.TestAggregateAnnotation2FieldsBean;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva
 * 4/2/17.
 */
public interface AggregateQueryProvider2TestRepository extends TestMongoRepository<TestAggregateAnnotation2FieldsBean, String>  {

  @Aggregate2(inputType = TestAggregateAnnotation2FieldsBean.class, genericType = true, outputBeanType = Map.class)
  @Match2(query = "{'randomAttribute2':?0}", order = 0)
  @Project2(query = "{'randomAttribute' : '$randomAttribute1', '_id' : 0}", order = 1)
  List<Map<String, String>> aggregateQueryWithMatchAndProjection(int value);

}
