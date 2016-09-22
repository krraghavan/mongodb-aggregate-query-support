// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.repository.test.aggregate;


import com.cisco.spring.data.mongodb.test.beans.TestAggregateAnnotation2FieldsBean;
import com.cisco.spring.data.mongodb.test.beans.annotation.*;

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