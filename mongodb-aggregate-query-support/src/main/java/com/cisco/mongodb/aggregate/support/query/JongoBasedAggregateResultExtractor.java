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

package com.cisco.mongodb.aggregate.support.query;

import com.cisco.mongodb.aggregate.support.api.ResultsExtractor;
import com.mongodb.DBObject;
import org.jongo.Jongo;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkolliva
 * 3/7/17.
 */
public class JongoBasedAggregateResultExtractor implements ResultsExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(JongoBasedAggregateResultExtractor.class);

  private final Jongo jongo;

  public JongoBasedAggregateResultExtractor(Jongo jongo) {
    this.jongo = jongo;
  }

  @Override
  public <T> T extractResults(DBObject result, Class<T> unwrapTo) {
    LOGGER.trace(">>>> JongoBasedAggregateResultExtractor::extractResults(single)");

    BsonDocument bsonDocument = Bson.createDocument(result);
    T retval = jongo.getMapper().getUnmarshaller().unmarshall(bsonDocument, unwrapTo);

    LOGGER.trace("<<<< JongoBasedAggregateResultExtractor::extractResults(single)");
    return retval;
  }

  @Override
  public <T> List<T> extractResults(Iterable<DBObject> result, Class<T> unwrapTo) {
    LOGGER.trace(">>>> JongoBasedAggregateResultExtractor::extractResults(iterable)");
    List<T> retval = new ArrayList<>();
    result.forEach(r -> retval.add(extractResults(r, unwrapTo)));
    LOGGER.trace("<<<< JongoBasedAggregateResultExtractor::extractResults(iterable)");
    return retval;
  }
}
