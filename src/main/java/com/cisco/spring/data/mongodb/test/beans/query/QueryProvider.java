// Copyright (c) 2015-2016 by Cisco Systems, Inc.
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
   * @return result key
   */
  String getQueryResultKey();
}
