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
import com.cisco.mongodb.aggregate.support.annotation.v2.Aggregate2;
import com.cisco.mongodb.aggregate.support.annotation.v2.Bucket2;
import com.cisco.mongodb.aggregate.support.test.beans.Artwork;
import com.cisco.mongodb.aggregate.support.test.beans.ArtworkBucketTestBean;
import com.cisco.mongodb.aggregate.support.test.beans.Histogram;
import com.cisco.mongodb.aggregate.support.test.beans.TestBucketBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva
 * 1/21/17.
 */
public interface TestBucketRepository extends TestMongoRepository<ArtworkBucketTestBean, String> {

  @Aggregate(inputType = ArtworkBucketTestBean.class, outputBeanType = HashMap.class,
             bucket = {
                 @Bucket(query = "{\n" +
                                 "      groupBy: \"$price\",\n" +
                                 "      boundaries: [ 0, 200, 400 ],\n" +
                                 "      default: \"Other\",\n" +
                                 "      output: {\n" +
                                 "        \"count\": { $sum: 1 },\n" +
                                 "        \"titles\" : { $push: \"$title\" }\n" +
                                 "      }\n" +
                                 "    }", order = 0)
             })
  List<Map<String, Object>> getBucketResults();

  @Aggregate2(inputType = ArtworkBucketTestBean.class, outputBeanType = HashMap.class)
  @Bucket2(query = "{\n" +
                  "      groupBy: \"$price\",\n" +
                  "      boundaries: [ 0, 200, 400 ],\n" +
                  "      default: \"Other\",\n" +
                  "      output: {\n" +
                  "        \"count\": { $sum: 1 },\n" +
                  "        \"titles\" : { $push: \"$title\" }\n" +
                  "      }\n" +
                  "    }", order = 0)
  List<Map<String, Object>> getBucketResults2();

}
