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
package com.github.krr.mongodb.aggregate.support.repository;


import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Group;
import com.github.krr.mongodb.aggregate.support.annotations.Match;
import com.github.krr.mongodb.aggregate.support.beans.TestGroupDataBean;
import com.github.krr.mongodb.aggregate.support.beans.TestGroupResultsBean;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by rkolliva on 10/19/2015.
 */
public interface ReactiveTestGroupRepository extends ReactiveTestMongoRepository<TestGroupDataBean, String> {

  // use only $price for $sum because fongo doesn't support all expressions.
  @Aggregate(inputType = TestGroupDataBean.class)
  @Group(order = 0, query = "{" +
                            "   \"_id\" : { \"month\": { \"$month\": \"$date\" }, \"day\": " +
                            "{ \"$dayOfMonth\": \"$date\" }, \"year\": { \"$year\": \"$date\" } }," +
                            "   \"totalPrice\": { \"$sum\": \"$price\" }," +
                            "   \"averageQuantity\": { \"$avg\": \"$quantity\" }," +
                            "   \"count\": { \"$sum\": 1 }" +
                            "}"
      )
  Flux<TestGroupResultsBean> salesBySalesDate();

  //Returns the total quantity for a given item.
  @Aggregate(inputType = TestGroupDataBean.class)
  @Match(query = "{'item' : '?0'}", order = 0)
  @Group(query = "{_id : null, totalQuantity : {$sum:'$quantity'}}", order = 1)
  Mono<Document> getTotalQuantityForOneItem(String itemName);
}
