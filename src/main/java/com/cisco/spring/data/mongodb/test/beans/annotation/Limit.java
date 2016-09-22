// Copyright (c) 2015-2016 by Cisco Systems, Inc.
/**
 * ------------------------------------------------------------------
 * <p>
 * Copyright (c) 2016 by Cisco Systems, Inc.
 * All rights reserved.
 * ------------------------------------------------------------------
 */
package com.cisco.spring.data.mongodb.test.beans.annotation;

/**
 * This $limit pipeline step in mongoDB aggregation query
 *
 * Created by sukhdevs on 4/8/16.
 */
public @interface Limit {
  String query();

  int order();
}