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
package com.cisco.mongodb.aggregate.support.annotation;

import org.bson.BsonDocument;

import java.lang.annotation.*;

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
  Class outputBeanType() default BsonDocument.class;

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

  /**
   * Support for $bucket aggregation pipeline operator.
   * Since Mongo 3.4
   *
   * @return - array of Bucket stages
   */
  Bucket[] bucket() default {};

  /**
   * Support for $addFields aggregation pipeline operator.
   * Since Mongo 3.4
   *
   * @return - array of $addFields stages
   */
  AddFields [] addFields() default {};

  /**
   * Support for $replaceRoot aggregation pipeline operator.
   * Since Mongo 3.4
   *
   * @return - array of $replaceRoot stages
   */
  ReplaceRoot[] replaceRoot() default {};

  /**
   * Support for $sort aggregation pipeline operator.
   *
   * @return - array of $replaceRoot stages
   */
  Sort[] sort() default {};


  /**
   * Support for $sort aggregation pipeline operator.
   * Since Mongo 3.4
   *
   * @return - array of $facet stages
   *
   */
  Facet[] facet() default {};

  /**
   * Implements the $count pipeline operator.
   *
   * @return - the count pipeline operator.  Only makes sense to have one of these.
   */
  Count [] count() default {};
}