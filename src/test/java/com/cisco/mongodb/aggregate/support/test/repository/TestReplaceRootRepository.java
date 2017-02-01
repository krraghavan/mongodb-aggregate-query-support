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
import com.cisco.mongodb.aggregate.support.annotation.Match;
import com.cisco.mongodb.aggregate.support.annotation.ReplaceRoot;
import com.cisco.mongodb.aggregate.support.annotation.Unwind;
import com.cisco.mongodb.aggregate.support.test.beans.TestReplaceRootBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva
 * This test assumes that all the data is in one repository on a real mongo
 * server.  The match queries ensure that the right data gets picked.
 *
 * @todo migrate to fongo and make it independent after fongo adds support
 * for these operations.
 *
 * 1/25/17.
 */
public interface TestReplaceRootRepository extends TestMongoRepository<TestReplaceRootBean, String> {

  @Aggregate(inputType = TestReplaceRootBean.class,
             match = {
                 @Match(query = "{ \"in_stock\" : { $exists: true } }", order = 0)
             },
             replaceRoot = {
                 @ReplaceRoot(query = "{ newRoot: \"$in_stock\" }", order = 1)
             }, outputBeanType = HashMap.class, genericType = true)
  List<Map<String, Integer>> getFruitInStockQuantity();

  @Aggregate(inputType = TestReplaceRootBean.class,
             replaceRoot = {
                 @ReplaceRoot(query = "{ newRoot: \"$pets\" }", order = 1)
             },
             match = {
                 @Match(query = "{ pets : { $exists: true } }", order = 0)
             },
             outputBeanType= HashMap.class, genericType = true)
  List<Map<String, Integer>> replaceRootWithMatch();

  @Aggregate(inputType = TestReplaceRootBean.class,
             replaceRoot = {
                 @ReplaceRoot(query = "{\n" +
                                      "         newRoot: {\n" +
                                      "            full_name: {\n" +
                                      "               $concat : [ \"$first_name\", \" \", \"$last_name\" ]\n" +
                                      "            }\n" +
                                      "         }\n" +
                                      "      }", order = 1)
             },
             match = {
                 @Match(query = "{ first_name : { $exists: true } }", order = 0)
             },
             outputBeanType= HashMap.class, genericType = true)
  List<Map<String, String>> replaceRootWithExpr();

  @Aggregate(inputType = TestReplaceRootBean.class,
             replaceRoot = {
                 @ReplaceRoot(query = "{ newRoot: \"$phones\"}", order = 3)
             },
             match = {
                 @Match(query = "{ aname : { $exists: true } }", order = 0),
                 @Match(query = "{ \"phones.cell\" : { $exists: true } }", order = 2)
             },
             unwind = {
                @Unwind(query = "\"$phones\"", order = 1)
             },
             outputBeanType= HashMap.class, genericType = true)
  List<Map<String,String>> replaceRootWithArray();
}
