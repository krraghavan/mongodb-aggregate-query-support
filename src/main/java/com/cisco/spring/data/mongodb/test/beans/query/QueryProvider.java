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
package com.cisco.spring.data.mongodb.test.beans.query;

/**
 * Created by rkolliva on 10/21/2015.
 */
public interface QueryProvider {

  /**
   * @return query
   */
  String getQuery();

  /**
   * @return output class
   */
  Class getOutputClass();

  /**
   * @return the collection name on which the query is executed
   */
  String getCollectionName();

  /**
   * @return true if the query is iterable
   */
  boolean isIterable();

  /**
   * @return true if the return type is a collection and
   * false if return type is not a collection
   */
  boolean returnCollection();

  /**
   * @return repository's aggregate function's return type
   */
  Class getMethodReturnType();

  /**
   * @return result key
   */
  String getQueryResultKey();
}
