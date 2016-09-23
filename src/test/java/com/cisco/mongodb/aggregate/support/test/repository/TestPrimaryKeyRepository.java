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



package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.test.beans.TestPrimaryKeyBean;
import com.cisco.mongodb.aggregate.support.annotation.Lookup;

import java.util.List;

/**
 * @author rkolliva.
 */
public interface TestPrimaryKeyRepository extends TestMongoRepository<TestPrimaryKeyBean, String> {

  @Aggregate(inputType = TestPrimaryKeyBean.class,
      lookup = {@Lookup(query = "{" +
                                "                \"from\": 'testForeignKeyBean'," +
                                "                \"localField\" : \"_id\"," +
                                "                \"foreignField\": \"foreignKey\"," +
                                "                \"as\": \"foreignKeyBeanList\"" +
                                "            }", order = 0)},
      outputBeanType = TestPrimaryKeyBean.class,
      genericType = true
  )
  List<TestPrimaryKeyBean> findAllPrimaryKeyBeans();
}
