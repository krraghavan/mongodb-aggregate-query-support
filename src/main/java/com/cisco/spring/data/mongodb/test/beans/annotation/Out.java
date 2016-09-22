// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans.annotation;

import java.lang.annotation.*;

/**
 * Created by camejavi on 6/9/2016
 * A pipeline step in an aggregate query. See {@link Aggregate} for further details
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Out {
  String query();
}
