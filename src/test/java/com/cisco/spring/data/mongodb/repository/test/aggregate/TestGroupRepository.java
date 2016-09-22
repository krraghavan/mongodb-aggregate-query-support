// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.repository.test.aggregate;


import com.cisco.spring.data.mongodb.test.beans.TestGroupDataBean;
import com.cisco.spring.data.mongodb.test.beans.TestGroupResultsBean;
import com.cisco.spring.data.mongodb.test.beans.annotation.Aggregate;
import com.cisco.spring.data.mongodb.test.beans.annotation.Group;
import com.cisco.spring.data.mongodb.test.beans.annotation.Match;

import java.util.List;

/**
 * Created by rkolliva on 10/19/2015.
 */
public interface TestGroupRepository extends TestMongoRepository<TestGroupDataBean, String> {

  // use only $price for $sum because fongo doesn't support all expressions.
  @Aggregate(inputType = TestGroupDataBean.class,
      group = @Group(order = 0, query = "{" +
                                        "   \"_id\" : { \"month\": { \"$month\": \"$date\" }, \"day\": " +
                                        "{ \"$dayOfMonth\": \"$date\" }, \"year\": { \"$year\": \"$date\" } }," +
                                        "   \"totalPrice\": { \"$sum\": \"$price\" }," +
                                        "   \"averageQuantity\": { \"$avg\": \"$quantity\" }," +
                                        "   \"count\": { \"$sum\": 1 }" +
                                        "}"
      ),
      outputBeanType = TestGroupResultsBean.class,
      genericType = true
  )
  List<TestGroupResultsBean> salesBySalesDate();

  //Returns the total quantity for a given item.
  @Aggregate(inputType = TestGroupDataBean.class,
      match = @Match(query = "{'item' : '?0'}", order = 0),
      group = @Group(query = "{_id : null, totalQuantity : {$sum:'$quantity'}}", order = 1),
      resultKey = "totalQuantity",
      outputBeanType = Integer.class
  )
  Integer getTotalQuantityForOneItem(String itemName);
}
