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
import com.cisco.mongodb.aggregate.support.test.beans.TestLongBean;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by rkolliva
 * 5/13/17.
 */
public interface TestLongRepository extends MongoRepository<TestLongBean, String> {

  @Aggregate2(name = "getRandomLong", inputType = TestLongBean.class, outputBeanType = TestLongBean.class)
  @Match2(query = "{'randomLong' : #}", order = 0)
  TestLongBean getRandomLong(Long longValue);

  @Aggregate2(name = "queryWithMixOfSpringAndJongoPlaceholders", inputType = TestLongBean.class,
              outputBeanType = TestLongBean.class)
  @Match2(query = "{'randomString' : ?0, 'randomLong' : #}", order = 0)
  TestLongBean queryWithMixOfSpringAndJongoPlaceholders(String randomString, long randomLong);

  @Aggregate2(name = "queryWithMixOfSpringAndJongoPlaceholdersRegardlessOfOrder", inputType = TestLongBean.class,
              outputBeanType = TestLongBean.class)
  @Match2(query = "{'randomString' : ?1, 'randomLong' : #}", order = 0)
  TestLongBean queryWithMixOfSpringAndJongoPlaceholdersRegardlessOfOrder(long randomLong, String randomString);

  @Aggregate2(name = "queryWithMultipleTemplateEnginePlaceholders", inputType = TestLongBean.class,
              outputBeanType = TestLongBean.class)
  @Match2(query = "{'randomLong' : #, 'randomLong2' : #}", order = 0)
  TestLongBean queryWithMultipleTemplateEnginePlaceholders(long randomLong, long randomLong2);

  @Aggregate2(name = "queryWithMultipleTemplateEnginePlaceholdersAndSpringPlaceholders", inputType = TestLongBean.class,
              outputBeanType = TestLongBean.class)
  @Match2(query = "{'randomString':?1, 'randomLong' : #, 'randomLong2' : #}", order = 0)
  TestLongBean queryWithMultipleTemplateEnginePlaceholdersAndSpringPlaceholders(long randomLong, String springPlaceholder,
                                                                                long randomLong2);
}
