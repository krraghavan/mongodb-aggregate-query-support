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

package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Sort;
import com.github.krr.mongodb.aggregate.support.annotations.Unwind;
import com.github.krr.mongodb.aggregate.support.beans.ArtworkSortTestBean;
import com.github.krr.mongodb.aggregate.support.beans.TestSortResultsBean;
import reactor.core.publisher.Flux;

/**
 * Created by rkolliva
 * 1/26/17.
 */

public interface ReactiveTestSortRepository extends ReactiveTestMongoRepository<ArtworkSortTestBean, String> {

  @Aggregate(inputType = ArtworkSortTestBean.class)
  @Unwind(query = "\"$tags\"", order = 0)
  @Sort(query = "{\"tags\" : 1}", order = 1)
  Flux<TestSortResultsBean> sortByTags();
}
