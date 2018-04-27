package com.github.krr.mongodb.aggregate.support.query;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.github.krr.mongodb.aggregate.support.bsoncodecs.ObjectIdAsStringCodecProvider;
import com.github.krr.mongodb.aggregate.support.exceptions.MongoQueryException;
import com.mongodb.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.ByteBufferBsonInput;
import org.bson.io.OutputBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongodb.AggregationOptions.builder;
import static java.nio.ByteBuffer.wrap;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@SuppressWarnings("Duplicates")
public class MongoNativeJavaDriverQueryExecutor implements MongoQueryExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoNativeJavaDriverQueryExecutor.class);

  private static final MapCodec MAP_CODEC = new MapCodec();

  private static final String MONGO_V3_6_VERSION = "3.6";
  public static final String RESULTS = "results";

  @SuppressWarnings("FieldCanBeLocal")
  private final boolean isMongo360OrLater;

  private final MongoOperations mongoOperations;

  public MongoNativeJavaDriverQueryExecutor(MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
    CommandResult result = mongoOperations.executeCommand("{buildinfo:1}");
    this.isMongo360OrLater = ((String) result.get("version")).startsWith(MONGO_V3_6_VERSION);
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  @Override
  public Object executeQuery(QueryProvider queryProvider) throws MongoQueryException {
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
                                                                    .maxTime(queryProvider.getMaxTimeMS(),
                                                                             TimeUnit.MILLISECONDS);
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
    // user wants to deserialize to a new class.
    // this registers the codec.
    ClassModel<T> classModel = ClassModel.builder(outputClass).build();
    PojoCodecProvider.Builder builder = PojoCodecProvider.builder().register(classModel).automatic(true);
    // ObjectIdAsString needs to be the first one so that we can use the ObjectIdAsString codec
    // to deserialize @Id fields as strings.
    CodecRegistry codecRegistry = fromProviders(new BsonValueCodecProvider(),
                                                new ObjectIdAsStringCodecProvider(),
                                                new ValueCodecProvider(),
                                                new MapCodecProvider(),
                                                new IterableCodecProvider(),
                                                new UuidCodecProvider(UuidRepresentation.JAVA_LEGACY),
                                                new DocumentCodecProvider(),
                                                builder.build());
    Codec codec = codecRegistry.get(outputClass);
    OutputBuffer encoded = encode(MAP_CODEC, d.toMap());
    return (T) decode(codec, encoded);
  }

  @SuppressWarnings("unchecked")
  private <T> T decode(Codec codec, OutputBuffer buffer) {
    BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(wrap(buffer.toByteArray()))));
    return (T) codec.decode(reader, DecoderContext.builder().build());
  }

  private <T> OutputBuffer encode(final Codec<T> codec, final T value) {
    OutputBuffer buffer = new BasicOutputBuffer();
    BsonWriter writer = new BsonBinaryWriter(buffer);
    codec.encode(writer, value, EncoderContext.builder().build());
    return buffer;
  }
}
