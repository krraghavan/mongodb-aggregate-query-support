// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans.annotation;

import java.lang.annotation.*;

/**
 * Created by rkolliva on 10/9/2015.
 * <p>
 * Annotation used to support Aggregation queries in MongoDB.  Using this
 * tests on top of any repository interface method will allow complex
 * aggregation queries to be specified without the need for providing any
 * implementation.  See the JavaDoc on each method and the unit test on
 * {AggregateTest} for more details on the usage of this tests.
 * <p>
 * The different pipeline steps are specified as an array of annotations of
 * each type that are currently supported
 *
 * @see Match
 * @see Project
 * @see Group
 * @see Unwind
 * @see Lookup
 * @see Limit
 * <p>
 * Each pipeline tests specifies the query and the order in which it is
 * to be executed in the pipeline.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Aggregate {

  /**
   * @return The class name of the input collection on which the aggregation pipeline starts
   */
  Class inputType();

  /**
   * @return true if the return type of the aggregate operation is a generic class (List/Map)
   */
  boolean genericType() default false;

  /**
   * @return - the class name of the return type.  If the return type is a list then this class
   * is the type of each list element
   */
  Class outputBeanType();

  /**
   * @return - the name of the attribute that should be extracted from the result if we need anything specific
   * By default whole object will be considered when it is empty
   */
  String resultKey() default "";

  /**
   * @return - list of projection pipeline steps in the aggregation
   */
  Project[] project() default {};

  /**
   * @return - list of group pipeline steps in the aggregation
   */
  Group[] group() default {};

  /**
   * @return - list of unwind pipeline steps in the aggreation
   */
  Unwind[] unwind() default {};

  /**
   * @return - list of match queries in the aggregation
   */
  Match[] match() default {};

  Lookup[] lookup() default {};

  Limit[] limit() default {};

  Out out() default @Out(query = "");

}