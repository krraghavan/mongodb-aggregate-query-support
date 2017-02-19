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

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.annotation.Bucket;
import com.cisco.mongodb.aggregate.support.test.beans.Histogram;
import com.cisco.mongodb.aggregate.support.test.beans.TestBucketBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by rkolliva
 * 1/21/17.
 */
@Repository("bucketTestRepository")
public interface TestBucketRepository extends TestMongoRepository<TestBucketBean, String> {

  @Aggregate(inputType = TestBucketBean.class, outputBeanType = Histogram.class,
             bucket = {
      @Bucket(query = "{\"groupBy\" : \"$count\",\n" +
                      " \"boundaries\" : [0, 60, 75],\n" +
                      " \"default\" : \"moreThan75\",\n" +
                      " \"output\" : {\n" +
                      "   \"count\" : {$sum : 1},\n" +
                      "   \"model\" : {$push : \"$model\"}\n" +
                      "  }" +
                      "}", order = 0)
             })
  List<Histogram> fixedBucket();

}
