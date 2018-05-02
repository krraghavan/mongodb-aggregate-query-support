package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.github.krr.mongodb.aggregate.support.deserializers.BsonDocumentObjectMapper;
import com.mongodb.AggregationOptions;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
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

@SuppressWarnings("Duplicates")
public class NonReactiveMongoNativeJavaDriverQueryExecutor implements MongoQueryExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(NonReactiveMongoNativeJavaDriverQueryExecutor.class);

  private static final ObjectMapper OBJECT_MAPPER = new BsonDocumentObjectMapper();

  private static final String MONGO_V3_6_VERSION = "3.6";

  private static final String RESULTS = "results";

  @SuppressWarnings("FieldCanBeLocal")
  private final boolean isMongo360OrLater;

  private final MongoOperations mongoOperations;

  public NonReactiveMongoNativeJavaDriverQueryExecutor(MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
    Document result = mongoOperations.executeCommand("{buildinfo:1}");
    this.isMongo360OrLater = ((String) result.get("version")).startsWith(MONGO_V3_6_VERSION);
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  @Override
  public Object executeQuery(QueryProvider queryProvider) {
    List<String> queries = queryProvider.getPipelines();
    Iterator<String> iterator = queries.iterator();
    int i = 0;

    String collectionName = queryProvider.getCollectionName();
    List<Bson> pipelineStages = new ArrayList<>();
    while (iterator.hasNext()) {
      String query = iterator.next();
      LOGGER.trace("Processing query string {} for pipeline stage {}", query, i++);
      Bson document = BsonDocument.parse(query);
      pipelineStages.add(document);
    }
    // run the pipeline and return a flux.
    MongoCollection<Document> collection = mongoOperations.getCollection(collectionName);

    AggregationOptions.Builder aggregationOptionsBuilder = builder().allowDiskUse(queryProvider.isAllowDiskUse())
                                                                    .maxTime(queryProvider.getMaxTimeMS(), MILLISECONDS);
    if (isMongo360OrLater) {
      // after 3.6 CURSOR MODE is mandatory
      aggregationOptionsBuilder.outputMode(AggregationOptions.OutputMode.CURSOR);
      LOGGER.debug("Mongo 3.6 detected - will use cursor mode for aggregate output");
    }
    // execute the query
    AggregateIterable<Document> aggregateIterable = collection.aggregate(pipelineStages);
    try (MongoCursor<Document> cursor = aggregateIterable.iterator()) {
      // e.g. a pipeline with an @Out stage would not have any return value.
      if (isVoidReturnType(queryProvider)) {
        return null;
      }
      Class outputClass = queryProvider.getOutputClass();
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

    return null;
  }

  private boolean isVoidReturnType(QueryProvider queryProvider) {
    return Void.class.isAssignableFrom(queryProvider.getMethodReturnType()) ||
           "void".equals(queryProvider.getMethodReturnType().getName());
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  private Object getPageableResults(QueryProvider<Pageable> queryProvider, MongoCursor<Document> cursor) {
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
        Document dbObject = cursor.next();
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
          Document dbObject = getDocument(o);
          Object results = dbObject.get(RESULTS);
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
  private Object getNonPageResults(QueryProvider<Pageable> queryProvider, MongoCursor<Document> cursor) {
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
          cursor.forEachRemaining(d -> retval.add(getValueFromDocument(resultKey, d)));
        }
        return retval;
      }
      else {
        if (cursor.hasNext()) {
          Document next = cursor.next();
          return getValueFromDocument(resultKey, next);
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
          Document document = getDocument(o);
          if (queryProvider.isPageable()) {
            // even though we're not returning a Page, the query itself could involve a Pageable.
            // in this case we'll throw away the totalResultSetCount from the returned results.
            Object results = document.get(RESULTS);
            Assert.isAssignable(List.class, results.getClass(), "Expecting a list of results");
            extractDeserializedListFromResults(outputClass, retval, (List) results);
            Assert.isTrue(!cursor.hasNext(), "For pageable type we only expect one record");
          }
          else {
            // returning a collection for non-paged complex objects.
            retval.add(deserialize(outputClass, document));
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

  private Document getDocument(Object o) {
    Assert.isAssignable(Document.class, o.getClass(), "Expecting DBObject type");
    return (org.bson.Document) o;
  }

  @SuppressWarnings("unchecked")
  private void extractDeserializedListFromResults(Class outputClass, List retval, List resultsList) {
    // the query involves a pageable type - the documents are in "results"
    // and the total count is in "totalResultSetCount".
    for (Object obj : resultsList) {
      retval.add(deserialize(outputClass, (Document) obj));
    }
  }

  private Object getValueFromDocument(String resultKey, Document dbObject) {
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
  private <T> T deserialize(Class<T> outputClass, Document d) {

    try {
      if(outputClass != Document.class) {
        return OBJECT_MAPPER.readValue(d.toJson(), outputClass);
      }
      return (T)d;
    }
    catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
