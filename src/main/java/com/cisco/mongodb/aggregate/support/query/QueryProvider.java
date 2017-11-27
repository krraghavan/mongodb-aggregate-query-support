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
package com.cisco.mongodb.aggregate.support.query;

import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by rkolliva on 10/21/2015.
 *
 */
public interface QueryProvider {

  // Define default value of two minutes on the server side for an aggregate
  // query to finish running before timing out.
  long DEFAULT_MAX_TIME_MS = 120_000L;

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

  /**
   * Returns true if the aggregate query returns a Pageable
   * @return true if aggregate query returns a pageable, false otherwise.
   */
  boolean isPageable();

  /**
   * Returns the pageable object associated with the aggregate query
   * @return - The Pageable object if the query is pageable null otherwise.
   *
   */
  Pageable getPageable();

  /**
   * @return true if the query was being served using the @Aggregate2 annotation, false otherwise
   */
  boolean isAggregate2();

  /**
   * @return true if the query is allowed to use disk space to avoid sort/groupBy space limitations
   */
  boolean isAllowDiskUse();

  /**
   * @return "time limit in milliseconds for processing operations on a cursor.
   * If you do not specify a value for maxTimeMS, operations will not time out.
   * A value of 0 explicitly specifies the default unbounded behavior."
   *
   * "MongoDB terminates operations that exceed their allotted time limit using
   * the same mechanism as db.killOp(). MongoDB only terminates an operation at
   * one of its designated interrupt points."
   *
   * Reference: https://docs.mongodb.com/manual/reference/command/aggregate/
   *
   */
  long getMaxTimeMS();

  /**
   * @return updated aggregate query with the new stage
   */
  List<String> modifyAggregateQueryPipeline(String newStage, int stage);

}
