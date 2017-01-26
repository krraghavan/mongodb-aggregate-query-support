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
import com.cisco.mongodb.aggregate.support.annotation.Sort;
import com.cisco.mongodb.aggregate.support.annotation.Unwind;
import com.cisco.mongodb.aggregate.support.test.beans.TestSortResultsBean;

import java.util.List;

/**
 * Created by rkolliva
 * 1/26/17.
 */

public interface TestSortRepository extends TestMongoRepository<TestSortResultsBean, String> {


  @Aggregate(inputType = TestSortResultsBean.class,
             unwind =
                 {
                     @Unwind(query = "\"$tags\"", order = 0)
                 },
             sort = {
                 @Sort(query = "{\"tags\" : 1}", order = 1)
             },
             outputBeanType = TestSortResultsBean.class
  )
  List<TestSortResultsBean> sortByTags();
}
