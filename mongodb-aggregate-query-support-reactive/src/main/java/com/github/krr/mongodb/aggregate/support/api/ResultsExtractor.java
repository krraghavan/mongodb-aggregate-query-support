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

package com.github.krr.mongodb.aggregate.support.api;

import com.mongodb.DBObject;

import java.util.List;

/**
 * Created by rkolliva
 * 3/7/17.
 *
 * Implementations of this interface give clients full control over the results that
 * have to be returned.  When the aggregate queries are declared to return a DBObject Type
 * callers have the option to provide a concrete implementation of this interface to extract
 * the result into any desired output class type.  Along with the placeholder replacement
 * capability for keys, this approach allows the same query structure to be able to deserialized
 * into different classes.
 */
public interface ResultsExtractor {

  <T> T extractResults(DBObject result, Class<T> unwrapTo);

  <T> List<T> extractResults(Iterable<DBObject> result, Class<T> unwrapTo);

}
