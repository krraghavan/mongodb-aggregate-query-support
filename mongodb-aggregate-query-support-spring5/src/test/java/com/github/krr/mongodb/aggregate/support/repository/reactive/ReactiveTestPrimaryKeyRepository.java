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



package com.github.krr.mongodb.aggregate.support.repository.reactive;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Lookup;
import com.github.krr.mongodb.aggregate.support.beans.TestPrimaryKeyBean;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveTestMongoRepository;
import reactor.core.publisher.Flux;

/**
 * @author rkolliva.
 */
public interface ReactiveTestPrimaryKeyRepository extends ReactiveTestMongoRepository<TestPrimaryKeyBean, String> {

  @Aggregate(inputType = TestPrimaryKeyBean.class, outputBeanType = TestPrimaryKeyBean.class)
  @Lookup(query = "{" +
                  "                \"from\": 'testForeignKeyBean'," +
                  "                \"localField\" : \"_id\"," +
                  "                \"foreignField\": \"foreignKey\"," +
                  "                \"as\": \"foreignKeyBeanList\"" +
                  "            }", order = 0)
  Flux<TestPrimaryKeyBean> findAllPrimaryKeyBeans();
}
