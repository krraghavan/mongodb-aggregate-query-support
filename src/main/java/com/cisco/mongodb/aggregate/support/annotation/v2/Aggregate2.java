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
package com.cisco.mongodb.aggregate.support.annotation.v2;

import org.bson.BsonDocument;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Aggregate2 {

  /**
   * A name assigned to the query to make it easy to identify it in debug messages
   *
   * @return - a name assigned to the aggregate query.  Not validated.  Just for
   * debuggability purposes.
   *
   */
  String name() default "unnamed";

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
   * @return true if the query is allowed to use disk space to avoid sort/groupBy space limitations
   */
  boolean isAllowDiskUse() default true;
}