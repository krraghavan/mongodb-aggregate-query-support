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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jongo.Aggregate;
import org.jongo.Aggregate.ResultsIterator;
import org.jongo.Jongo;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Created by rkolliva on 10/21/2015.
 * A query executor for test cases
 */
@SuppressWarnings({"unchecked", "squid:S2259"})
@Component
public class JongoQueryExecutor implements MongoQueryExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(JongoQueryExecutor.class);

  private static final String UNEXPECTED_NULL_AGGREGATE_QUERY = "Unexpected null aggregate query";

  private static final String QUERY_RETURN_ERROR_STR = "Query is expecting a single object to be returned but " +
                                                       "the result set had multiple rows";

  private final Jongo jongo;

  @Autowired
  public JongoQueryExecutor(Jongo jongo) {
    this.jongo = jongo;
  }

  @Override
  public Object executeQuery(QueryProvider queryProvider, Object... params) throws MongoQueryException {
    Iterator<String> iterator = (Iterator) queryProvider;
    int i = 0;
    Aggregate aggregate = null;

    while (iterator.hasNext()) {
      String query = iterator.next();
      if (i++ == 0) {
        aggregate = jongo.getCollection(queryProvider.getCollectionName()).aggregate(query, params);
      }
      else {
        Assert.notNull(aggregate, UNEXPECTED_NULL_AGGREGATE_QUERY);
        aggregate.and(query);
      }
    }
    Assert.notNull(aggregate, UNEXPECTED_NULL_AGGREGATE_QUERY);
    ResultsIterator resultsIterator = aggregate.as(HashMap.class);
    if (resultsIterator == null || !resultsIterator.hasNext() || Void.TYPE.equals(queryProvider.getMethodReturnType())) {
      return null;
    }
    final String resultKey = queryProvider.getQueryResultKey();
    if(isPageReturnType(queryProvider) && !queryProvider.isAggregate2()) {
      throw new IllegalArgumentException("Page can be used as a return type only with @Aggregate2 annotation");
    }
    if (!queryProvider.isPageable() || (queryProvider.isPageable() &&
                                        List.class.isAssignableFrom(queryProvider.getMethodReturnType()))) {
      return getNonPageResults(queryProvider, resultsIterator, resultKey);
    }
    else if (queryProvider.isPageable() && isPageReturnType(queryProvider)) {
      return getPageableResults(queryProvider, resultsIterator);
    }
    throw new MongoQueryException(QUERY_RETURN_ERROR_STR);
  }

  private boolean isPageReturnType(QueryProvider queryProvider) {
    return Page.class.isAssignableFrom(queryProvider.getMethodReturnType());
  }

  private Object getPageableResults(QueryProvider queryProvider, ResultsIterator resultsIterator) {
    // for pageable we get one entry always - which is a DBObject
    Map<String, Object> valueMap = (Map) resultsIterator.next();

    final List results = (List) valueMap.get("results");
    // we need to deserialize each of these.

    final List retval = new ArrayList();
    for (Object result : results) {
      unmarshallResult(queryProvider, retval, result);
    }
    final int count = (int)valueMap.get("totalResultSetCount");
    // return type is pageable

    return new PageImpl(retval, queryProvider.getPageable(), count);
  }

  private Object getNonPageResults(QueryProvider queryProvider, Iterator resultsIterator,
                                   String resultKey) throws MongoQueryException {
    final List retval = new ArrayList();
    resultsIterator.forEachRemaining(o -> {
      LOGGER.debug("Got object {}", o.toString());
      Assert.isTrue(o instanceof Map);
      Map<String, Object> instanceMap = (Map) o;
      Object valueObject;
      //If there is no result key, we should consider the whole return values map
      //If not consider only the value in result key
      if (resultKey.isEmpty()) {
        valueObject = instanceMap;
      }
      else {
        valueObject = instanceMap.get(resultKey);
      }
      //If the result is a map(by default map will be returned) we need to
      // deserialize. If not, then it is a generic type(Eg. String, Integer)
      // which does not need deserialization
      unmarshallResult(queryProvider, retval, valueObject);
    });
    if (queryProvider.returnCollection()) {
      return retval;
    }
    else if (!queryProvider.returnCollection() && retval.size() == 1) {
      return retval.get(0);
    }
    throw new MongoQueryException("Query is expecting a single object to be returned but the result set had multiple rows");
  }

  private void unmarshallResult(QueryProvider queryProvider, List retval, Object valueObject) {
    Object resultObject;
    if (valueObject instanceof Map) {
      Map value = (Map) valueObject;
      DBObject dbObject = new BasicDBObject(value);
      Class outputClass = queryProvider.getOutputClass();
      if (outputClass == DBObject.class) {
        resultObject = dbObject;
      }
      else {
        BsonDocument bsonDocument = Bson.createDocument(dbObject);
        resultObject = jongo.getMapper().getUnmarshaller().unmarshall(bsonDocument, outputClass);
      }
    }
    else {
      resultObject = valueObject;
    }
    retval.add(resultObject);
  }
}
