package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.github.krr.mongodb.aggregate.support.exceptions.MongoQueryException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.json.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofMillis;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@SuppressWarnings({"Duplicates", "unused"})
public class MongoNativeJavaDriverQueryExecutor extends AbstractQueryExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoNativeJavaDriverQueryExecutor.class);

  private static final String RESULTS = "results";

  public MongoNativeJavaDriverQueryExecutor(MongoOperations mongoOperations) {
    super(mongoOperations);
  }

  @SuppressWarnings("WeakerAccess")
  public MongoNativeJavaDriverQueryExecutor(MongoOperations mongoOperations, ObjectMapper objectMapper) {
    super(mongoOperations, objectMapper);
  }

  @Override
  public Object executeQuery(QueryProvider queryProvider) {
    List<String> queries = queryProvider.getPipelines();

    Iterator<String> iterator = queries.iterator();
    int i = 0;

    String collectionName = queryProvider.getCollectionName();
    List<BasicDBObject> pipelineStages = new ArrayList<>();
    AggregationOptions options = Aggregation.newAggregationOptions().allowDiskUse(queryProvider.isAllowDiskUse())
                                                .maxTime(ofMillis(queryProvider.getMaxTimeMS())).build();
    while (iterator.hasNext()) {
      String query = iterator.next();
      LOGGER.trace("Processing query string {} for pipeline stage {}", query, i++);
      try {
        BasicDBObject dbObject = BasicDBObject.parse(query);
        pipelineStages.add(dbObject);
      }
      catch (JsonParseException e) {
        LOGGER.error("Error parsing query string {} for pipeline stage {}", query, i++, e);
        throw e;
      }
    }

    MongoCollection<Document> collection = mongoOperations.getCollection(collectionName);
    // execute the query
    try (MongoCursor<Document> cursor = collection.aggregate(pipelineStages).allowDiskUse(queryProvider.isAllowDiskUse())
                                                  .maxTime(queryProvider.getMaxTimeMS(), TimeUnit.MILLISECONDS).cursor()) {
      if (isVoidReturnType(queryProvider)) {
        return null;
      }
      if (cursor.hasNext()) {
        if (!queryProvider.isPageable() || (queryProvider.isPageable() &&
                                            List.class.isAssignableFrom(queryProvider.getMethodReturnType()))) {
          return getNonPageResults(queryProvider, cursor);
        }
        else if (queryProvider.isPageable() && isPageReturnType(queryProvider)) {
          return getPageableResults(queryProvider, cursor);
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Error executing query {}.  Operation failed with error", pipelineStages, e);
      throw new MongoQueryException(e);
    }
    return null;
  }

  private boolean isVoidReturnType(QueryProvider queryProvider) {
    return Void.class.isAssignableFrom(queryProvider.getMethodReturnType()) ||
           "void".equals(queryProvider.getMethodReturnType().getName());
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  private <T> Object getPageableResults(QueryProvider queryProvider, MongoCursor<Document> cursor) {
    Class<?> returnClass = queryProvider.getMethodReturnType();
    boolean isCollectionTypeReturn = Iterable.class.isAssignableFrom(returnClass);
    final Class<T> outputClass = queryProvider.getOutputClass();
    Assert.isTrue(!isCollectionTypeReturn || isCollectionTypeReturn && Page.class.isAssignableFrom(returnClass),
                  "Only page return type is supported. " + returnClass.getName() + " is not assignable to page");
    String resultKey = queryProvider.getQueryResultKey();

    // we're returning a page full of data with a Pageable argument.  The query always returns
    // a single object with "results" and "totalResultSetCount".  We just package that in a
    // Page and return.
    Pageable pageable = queryProvider.getPageable();
    if (BeanUtils.isSimpleValueType(outputClass)) {
      // either primitives/String or list of such objects.
      List<T> pageContents = new ArrayList<>();
      Page<?> retval = new PageImpl<>(new ArrayList<>(), pageable, 0);
      // expect only one object.
      if (cursor.hasNext()) {
        Document dbObject = cursor.next();
        Object results = dbObject.get(RESULTS);
        if(results == null) {
          return null;
        }
        Assert.isAssignable(DBObject.class, results.getClass(), "Expecting DBObject type");
        Assert.isAssignable(List.class, results.getClass(), "Expecting a list of results");
        extractDeserializedListFromResults(outputClass, pageContents, (List<Document>) results);
        retval = new PageImpl<>(pageContents, pageable, (int) dbObject.get("totalResultSetCount"));
      }
      Assert.isTrue(!cursor.hasNext(), "Expecting only one record in paged query");
      return retval;
    }
    else {
      // deserialize into complex object
      final Object[] returnValue = new Object[1];
      if (isCollectionTypeReturn) {
        List<T> pageContents = new ArrayList<>();
        cursor.forEachRemaining(d -> {
          Object o = StringUtils.isNotEmpty(resultKey) ? d.get(resultKey) : d;
          // If the query has a pageable in it the results would be returned as a DBObject
          // but the actual results are in the "results" key.  In this case we're not returning
          // a page but a list so we'll just throw away the totals
          Document dbObject = getDbObject(o);
          Object results = dbObject.get(RESULTS);
          Assert.isAssignable(List.class, results.getClass(), "Expecting a list of results");
          extractDeserializedListFromResults(outputClass, pageContents, (List<Document>) results);
          returnValue[0] = new PageImpl<>(pageContents, pageable, (int) dbObject.get("totalResultSetCount"));
          Assert.isTrue(!cursor.hasNext(), "For pageable type we only expect one record");
        });
        return returnValue[0];
      }
    }
    throw new IllegalStateException("Unexpected exit from block");
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  /*
   * Called when results are not paged or if paged, the return is to a list type.
   */
  private <T> Object getNonPageResults(QueryProvider queryProvider, MongoCursor<Document> cursor) {
    Class<?> returnClass = queryProvider.getMethodReturnType();
    boolean isCollectionTypeReturn = Iterable.class.isAssignableFrom(returnClass);
    final Class<T> outputClass = queryProvider.getOutputClass();
    Assert.isTrue(!isCollectionTypeReturn || isCollectionTypeReturn && List.class.isAssignableFrom(returnClass),
                  "Only list return type is supported. " + returnClass.getName() + " is not assignable to list");
    String resultKey = queryProvider.getQueryResultKey();

    if (BeanUtils.isSimpleValueType(outputClass)) {
      // either primitives/String or list of such objects.
      if (isCollectionTypeReturn) {
        List<Object> retval = new ArrayList<>();
        while (cursor.hasNext()) {
          cursor.forEachRemaining(d -> retval.add(getValueFromDbObject(resultKey, d)));
        }
        return retval;
      }
      else {
        if (cursor.hasNext()) {
          Document next = cursor.next();
          return getValueFromDbObject(resultKey, next);
        }
      }
    }
    else {
      // deserialize into complex object.  Single of list of complex objects
      if (isCollectionTypeReturn) {
        List<T> retval = new ArrayList<>();
        cursor.forEachRemaining(d -> {
          // resultKey would typically be empty for paged results.  But if the
          // query projects other values, the resultKey provides a way to extract
          // the results piece
          Object o = StringUtils.isNotEmpty(resultKey) ? d.get(resultKey) : d;
          // If the query has a pageable in it the results would be returned as a DBObject
          // but the actual results are in the "results" key.  In this case we're not returning
          // a page but a list so we'll just throw away the totals
          Document dbObject = getDbObject(o);
          if (queryProvider.isPageable()) {
            // even though we're not returning a Page, the query itself could involve a Pageable.
            // in this case we'll throw away the totalResultSetCount from the returned results.
            Object results = dbObject.get(RESULTS);
            Assert.isAssignable(List.class, results.getClass(), "Expecting a list of results");
            //noinspection rawtypes
            extractDeserializedListFromResults(outputClass, retval, (List) results);
            Assert.isTrue(!cursor.hasNext(), "For pageable type we only expect one record");
          }
          else {
            // returning a collection for non-paged complex objects.
            retval.add(deserialize(outputClass, dbObject));
          }
        });
        return retval;
      }
      else {
        Document next = cursor.next();
        Document d = StringUtils.isNotEmpty(resultKey) ? (Document) next.get(resultKey) : next;
        Assert.isTrue(!cursor.hasNext(), "Return type was for a single object but query returned multiple records");
        return deserialize(outputClass, d);
      }
    }
    return null;
  }

  private Document getDbObject(Object o) {
    Assert.isAssignable(Document.class, o.getClass(), "Expecting Document type");
    return (Document) o;
  }

  private <T> void extractDeserializedListFromResults(Class<T> outputClass, List<T> retval, List<Document> resultsList) {
    // the query involves a pageable type - the documents are in "results"
    // and the total count is in "totalResultSetCount".
    for (Document obj : resultsList) {
      retval.add(deserialize(outputClass, obj));
    }
  }

  private Object getValueFromDbObject(String resultKey, Document dbObject) {
    Object retval;
    if (StringUtils.isNotEmpty(resultKey)) {
      retval = dbObject.get(resultKey);
    }
    else {
      retval = dbObject;
    }
    return retval;
  }

  private boolean isPageReturnType(QueryProvider queryProvider) {
    return Page.class.isAssignableFrom(queryProvider.getMethodReturnType());
  }

  private <T> T deserialize(Class<T> outputClass, Document d) {

    try {
      return objectMapper.readValue(d.toJson(), outputClass);
    }
    catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
