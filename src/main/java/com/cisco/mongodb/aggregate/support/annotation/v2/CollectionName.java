/*
 *  Copyright (c) 2018 the original author or authors.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rkolliva
 * 1/2/18.
 *
 * An annotation that indicates that the string argument it prefixes represents the name of a
 * collection.  If the argument that this annotation decorates is not a string, the toString() method
 * of that argument will be called to derive the string value.  Depending on the intent, this may or
 * may not yield the desired intent.  When this annotation is used the aggregate query is executed
 * on the named collection rather than the name derived from the interface.  This allows the
 * aggregate query to be reused across different collections that contain the same types of documents
 * A useful use case for this would be (for instance) when very large collections get split into multiple
 * smaller collections for performance reasons.
 *
 * It is an error to give this annotation on multiple parameters - a runtime exception will be thrown
 * if this situation occurs.
 *
 * @since 0.7.26
 *
 * Note: Works only with @{@link Aggregate2} style annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CollectionName {

}
