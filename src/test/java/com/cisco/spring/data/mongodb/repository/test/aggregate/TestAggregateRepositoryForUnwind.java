// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.repository.test.aggregate;


import com.cisco.spring.data.mongodb.test.beans.TestUnwindAggregateAnnotationBean;
import com.cisco.spring.data.mongodb.test.beans.annotation.Aggregate;
import com.cisco.spring.data.mongodb.test.beans.annotation.Unwind;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva on 10/19/2015.
 */
public interface TestAggregateRepositoryForUnwind extends
                                                  TestMongoRepository<TestUnwindAggregateAnnotationBean, String> {

  @Aggregate(inputType = TestUnwindAggregateAnnotationBean.class,
      genericType = true,
      outputBeanType = Map.class,
      unwind = {@Unwind(query = "'$randomListOfStrings'", order = 0)}
  )
  List<Map> aggregateQueryWithOnlyUnwind(int intValue1);
}
