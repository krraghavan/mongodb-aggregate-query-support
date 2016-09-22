// Copyright (c) 2015-2016 by Cisco Systems, Inc.
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
