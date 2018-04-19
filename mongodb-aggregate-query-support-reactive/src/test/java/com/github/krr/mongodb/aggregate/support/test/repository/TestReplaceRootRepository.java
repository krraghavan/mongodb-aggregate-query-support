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

package com.github.krr.mongodb.aggregate.support.test.repository;

import com.github.krr.mongodb.aggregate.support.annotation.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotation.Match;
import com.github.krr.mongodb.aggregate.support.annotation.ReplaceRoot;
import com.github.krr.mongodb.aggregate.support.annotation.Unwind;
import com.github.krr.mongodb.aggregate.support.test.beans.TestReplaceRootBean;
import org.bson.Document;
import reactor.core.publisher.Flux;

/**
 * Created by rkolliva
 *
 * 1/25/17.
 */
public interface TestReplaceRootRepository extends TestMongoRepository<TestReplaceRootBean, String> {

  @Aggregate(inputType = TestReplaceRootBean.class)
  @Match(query = "{ inStock : { $exists: true } }", order = 0)
  @ReplaceRoot(query = "{ newRoot: \"$inStock\" }", order = 1)
  Flux<Document> getFruitInStockQuantity();

  @Aggregate(inputType = TestReplaceRootBean.class)
  @ReplaceRoot(query = "{ newRoot: \"$pets\" }", order = 1)
  @Match(query = "{ pets : { $exists: true } }", order = 0)
  Flux<Document> replaceRootWithMatch();

  @Aggregate(inputType = TestReplaceRootBean.class)
  @ReplaceRoot(query = "{\n" +
                       "         newRoot: {\n" +
                       "            fullName: {\n" +
                       "               $concat : [ \"$firstName\", \" \", \"$lastName\" ]\n" +
                       "            }\n" +
                       "         }\n" +
                       "      }", order = 1)
  @Match(query = "{ firstName : { $exists: true } }", order = 0)
  Flux<Document> replaceRootWithExpr();

  @Aggregate(inputType = TestReplaceRootBean.class)
  @ReplaceRoot(query = "{ newRoot: \"$phones\"}", order = 3)
  @Match(query = "{ aname : { $exists: true } }", order = 0)
  @Match(query = "{ \"phones.cell\" : { $exists: true } }", order = 2)
  @Unwind(query = "\"$phones\"", order = 1)
  Flux<Document> replaceRootWithArray();
}
