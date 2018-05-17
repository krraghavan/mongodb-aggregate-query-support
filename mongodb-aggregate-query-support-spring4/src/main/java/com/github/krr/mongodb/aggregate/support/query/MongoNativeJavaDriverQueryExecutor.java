package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.mongodb.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mongodb.AggregationOptions.builder;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@SuppressWarnings({"Duplicates", "unused"})
public class MongoNativeJavaDriverQueryExecutor extends AbstractQueryExecutor<MongoOperations> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoNativeJavaDriverQueryExecutor.class);

  private static final String MONGO_V3_6_VERSION = "3.6";

  private static final String RESULTS = "results";

  @SuppressWarnings("FieldCanBeLocal")
  private boolean isMongo360OrLater;

  public MongoNativeJavaDriverQueryExecutor(MongoOperations mongoOperations) {
    super(mongoOperations);
    initialize(mongoOperations);
  }

  @SuppressWarnings("WeakerAccess")
  public MongoNativeJavaDriverQueryExecutor(MongoOperations mongoOperations, ObjectMapper objectMapper) {
    super(mongoOperations, objectMapper);
    initialize(mongoOperations);
  }

  private void initialize(MongoOperations mongoOperations) {
    CommandResult result = mongoOperations.executeCommand("{buildinfo:1}");
    this.isMongo360OrLater = ((String) result.get("version")).startsWith(MONGO_V3_6_VERSION);
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  @Override
  public Object executeQuery(QueryProvider queryProvider) {
    List<String> queries = queryProvider.getPipelines();
    Iterator<String> iterator = queries.iterator();
    int i = 0;

    String collectionName = queryProvider.getCollectionName();
    List<DBObject> pipelineStages = new ArrayList<>();
    while (iterator.hasNext()) {
      String query = iterator.next();
      LOGGER.trace("Processing query string {} for pipeline stage {}", query, i++);
      DBObject dbObject = BasicDBObject.parse(query);
      pipelineStages.add(dbObject);
    }

    AggregationOptions.Builder aggregationOptionsBuilder = builder().allowDiskUse(queryProvider.isAllowDiskUse())
                                                                    .maxTime(queryProvider.getMaxTimeMS(), MILLISECONDS);
    if (isMongo360OrLater) {
      // after 3.6 CURSOR MODE is mandatory
      aggregationOptionsBuilder.outputMode(AggregationOptions.OutputMode.CURSOR);
      LOGGER.debug("Mongo 3.6 detected - will use cursor mode for aggregate output");
    }
    DBCollection collection = mongoOperations.getCollection(collectionName);
    // execute the query
    Cursor cursor = collection.aggregate(pipelineStages, aggregationOptionsBuilder.build());
    try {
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
    finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return null;
  }

  private boolean isVoidReturnType(QueryProvider queryProvider) {
    return Void.class.isAssignableFrom(queryProvider.getMethodReturnType()) ||
           "void".equals(queryProvider.getMethodReturnType().getName());
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  private Object getPageableResults(QueryProvider<Pageable> queryProvider, Cursor cursor) {
    Class returnClass = queryProvider.getMethodReturnType();
    boolean isCollectionTypeReturn = Iterable.class.isAssignableFrom(returnClass);
    final Class outputClass = queryProvider.getOutputClass();
    Assert.isTrue(!isCollectionTypeReturn || isCollectionTypeReturn && Page.class.isAssignableFrom(returnClass),
                  "Only page return type is supported. " + returnClass.getName() + " is not assignable to page");
    String resultKey = queryProvider.getQueryResultKey();

    // we're returning a page full of data with a Pageable argument.  The query always returns
    // a single object with "results" and "totalResultSetCount".  We just package that in a
    // Page and return.
    Pageable pageable = queryProvider.getPageable();
    if (BeanUtils.isSimpleValueType(outputClass)) {
      // either primitives/String or list of such objects.
      List pageContents = new ArrayList<>();
      Page retval = new PageImpl(new ArrayList(), pageable, 0);
      // expect only one object.
      if (cursor.hasNext()) {
        DBObject dbObject = cursor.next();
        Object results = dbObject.get(RESULTS);
        if(results == null) {
          return null;
        }
        Assert.isAssignable(DBObject.class, results.getClass(), "Expecting DBObject type");
        Assert.isAssignable(List.class, results.getClass(), "Expecting a list of results");
        extractDeserializedListFromResults(outputClass, pageContents, (List) results);
        retval = new PageImpl(pageContents, pageable, (int) dbObject.get("totalResultSetCount"));
      }
      Assert.isTrue(!cursor.hasNext(), "Expecting only one record in paged query");
      return retval;
    }
    else {
      // deserialize into complex object
      final Object[] returnValue = new Object[1];
      if (isCollectionTypeReturn) {
        List pageContents = new ArrayList<>();
        cursor.forEachRemaining(d -> {
          Object o = StringUtils.isNotEmpty(resultKey) ? d.get(resultKey) : d;
          // If the query has a pageable in it the results would be returned as a DBObject
          // but the actual results are in the "results" key.  In this case we're not returning
          // a page but a list so we'll just throw away the totals
          DBObject dbObject = getDbObject(o);
          Object results = dbObject.get(RESULTS);
          Assert.isAssignable(DBObject.class, results.getClass(), "Expecting DBObject type");
          Assert.isAssignable(List.class, results.getClass(), "Expecting a list of results");
          extractDeserializedListFromResults(outputClass, pageContents, (List) results);
          returnValue[0] = new PageImpl(pageContents, pageable, (int) dbObject.get("totalResultSetCount"));
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
  private Object getNonPageResults(QueryProvider<Pageable> queryProvider, Cursor cursor) {
    Class returnClass = queryProvider.getMethodReturnType();
    boolean isCollectionTypeReturn = Iterable.class.isAssignableFrom(returnClass);
    final Class outputClass = queryProvider.getOutputClass();
    Assert.isTrue(!isCollectionTypeReturn || isCollectionTypeReturn && List.class.isAssignableFrom(returnClass),
                  "Only list return type is supported. " + returnClass.getName() + " is not assignable to list");
    String resultKey = queryProvider.getQueryResultKey();

    if (BeanUtils.isSimpleValueType(outputClass)) {
      // either primitives/String or list of such objects.
      if (isCollectionTypeReturn) {
        List retval = new ArrayList<>();
        while (cursor.hasNext()) {
          cursor.forEachRemaining(d -> retval.add(getValueFromDbObject(resultKey, d)));
        }
        return retval;
      }
      else {
        if (cursor.hasNext()) {
          DBObject next = cursor.next();
          return getValueFromDbObject(resultKey, next);
        }
      }
    }
    else {
      // deserialize into complex object.  Single of list of complex objects
      if (isCollectionTypeReturn) {
        List retval = new ArrayList<>();
        cursor.forEachRemaining(d -> {
          // resultKey would typically be empty for paged results.  But if the
          // query projects other values, the resultKey provides a way to extract
          // the results piece
          Object o = StringUtils.isNotEmpty(resultKey) ? d.get(resultKey) : d;
          // If the query has a pageable in it the results would be returned as a DBObject
          // but the actual results are in the "results" key.  In this case we're not returning
          // a page but a list so we'll just throw away the totals
          DBObject dbObject = getDbObject(o);
          if (queryProvider.isPageable()) {
            // even though we're not returning a Page, the query itself could involve a Pageable.
            // in this case we'll throw away the totalResultSetCount from the returned results.
            Object results = dbObject.get(RESULTS);
            Assert.isAssignable(DBObject.class, results.getClass(), "Expecting DBObject type");
            Assert.isAssignable(List.class, results.getClass(), "Expecting a list of results");
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
        DBObject next = cursor.next();
        DBObject d = StringUtils.isNotEmpty(resultKey) ? (DBObject) next.get(resultKey) : next;
        Assert.isTrue(!cursor.hasNext(), "Return type was for a single object but query returned multiple records");
        return deserialize(outputClass, d);
      }
    }
    return null;
  }

  private DBObject getDbObject(Object o) {
    Assert.isAssignable(DBObject.class, o.getClass(), "Expecting DBObject type");
    return (DBObject) o;
  }

  @SuppressWarnings("unchecked")
  private void extractDeserializedListFromResults(Class outputClass, List retval, List resultsList) {
    // the query involves a pageable type - the documents are in "results"
    // and the total count is in "totalResultSetCount".
    for (Object obj : resultsList) {
      retval.add(deserialize(outputClass, (DBObject) obj));
    }
  }

  private Object getValueFromDbObject(String resultKey, DBObject dbObject) {
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

  @SuppressWarnings("unchecked")
  private <T> T deserialize(Class<T> outputClass, DBObject d) {

    try {
      Document document = new Document(d.toMap());
      return objectMapper.readValue(document.toJson(), outputClass);
    }
    catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
