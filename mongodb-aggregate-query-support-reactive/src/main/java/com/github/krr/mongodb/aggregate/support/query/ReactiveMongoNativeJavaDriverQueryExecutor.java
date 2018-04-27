package com.github.krr.mongodb.aggregate.support.query;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.github.krr.mongodb.aggregate.support.bsoncodecs.ObjectIdAsStringCodecProvider;
import com.mongodb.reactivestreams.client.AggregatePublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.apache.commons.lang3.StringUtils;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.ByteBufferBsonInput;
import org.bson.io.OutputBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * Created by rkolliva
 * 4/16/18.
 */

public class ReactiveMongoNativeJavaDriverQueryExecutor implements MongoQueryExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveMongoNativeJavaDriverQueryExecutor.class);

  private static final Codec<BsonDocument> DOCUMENT_CODEC = new BsonDocumentCodec();

  private static final String MONGO_V3_6_VERSION = "3.6";

  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final boolean isMongo360OrLater;

  private final ReactiveMongoOperations mongoOperations;

  public ReactiveMongoNativeJavaDriverQueryExecutor(ReactiveMongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
    Document result = mongoOperations.executeCommand("{buildinfo:1}").block();
    this.isMongo360OrLater = ((String)result.get("version")).startsWith(MONGO_V3_6_VERSION);
  }

  @Override
  public Object executeQuery(QueryProvider queryProvider) {

//    // convert the pipelines by parsing the JSON strings
//    Assert.isAssignable(Iterator.class, queryProvider.getClass(),
//                        "Query Provider must implement the iterator interface");
    Iterator iterator = queryProvider.getPipelines().iterator();
    int i = 0;

    String collectionName = queryProvider.getCollectionName();
    List<Bson> pipelineStages = new ArrayList<>();
    while (iterator.hasNext()) {
      String query = (String) iterator.next();
      LOGGER.trace("Processing query string {} for pipeline stage {}", query, i++);
      Bson document = BsonDocument.parse(query);
      pipelineStages.add(document);
    }
    // run the pipeline and return a flux.
    MongoCollection<Document> collection = mongoOperations.getCollection(collectionName);
    AggregatePublisher<Document> aggregatePublisher = collection.aggregate(pipelineStages);
    Class methodReturnType = queryProvider.getMethodReturnType();
    boolean isFlux = Flux.class.isAssignableFrom(methodReturnType);
    boolean isMono = Mono.class.isAssignableFrom(methodReturnType);
    boolean isFluxOrMono = isFlux || isMono;
    if (!isFluxOrMono) {
      throw new IllegalArgumentException("Method return type must be of Flux or Mono type");
    }
    Class<?> outputClass = queryProvider.getOutputClass();
    if (isFlux) {
      LOGGER.trace("Return type is Flux<{}>", outputClass);
      Flux<Document> retval = Flux.from(aggregatePublisher);
      if (outputClass != null) {
        return adaptPipeline(queryProvider, outputClass, retval);
      }
      return retval;
    }
    else {
      Mono<Document> mono = Mono.from(aggregatePublisher);
      if (outputClass != null) {
        LOGGER.trace("Return type is Mono<{}>", outputClass);
        return adaptPipeline(queryProvider, outputClass, mono);
      }
      return mono;
    }
  }

  private <T> Mono<T> adaptPipeline(QueryProvider queryProvider, Class<T> outputClass, Mono<Document> retval) {
    String key = queryProvider.getQueryResultKey();
    if (BeanUtils.isSimpleValueType(outputClass)) {
      return retval.map(d -> d.get(key, outputClass))
                   .doOnError(e -> LOGGER.error("Exception while extracting results from document for key {}", key, e));
    }
    return retval.map(d -> (Document) getDocumentForKey(key, d, false))
                 .map(d -> d.toBsonDocument(Document.class, CodecRegistries.fromCodecs(new DocumentCodec())))
                 .map((d) -> deserialize(outputClass, d))
                 .doOnError(e -> LOGGER.error("Exception while extracting results from document for key {}", key, e));
  }

  private Object getDocumentForKey(String key, Document d, boolean isFlux) {
    if (StringUtils.isEmpty(key)) {
      LOGGER.debug("No key specified - will return document");

      // if no key specified we just return this document.
      return d;
    }
    else if (isFlux) {
      Object o = d.get(key);
      if (Iterable.class.isAssignableFrom(o.getClass())) {
        LOGGER.debug("Returning iterable document from query results for key {}", key);
        return o;
      }
    }
    LOGGER.debug("Returning embedded document from query results for key {}", key);
    return d.get(key);
  }

  private <T> Flux<T> adaptPipeline(QueryProvider queryProvider, Class<T> outputClass, Flux<Document> retval) {
    String key = queryProvider.getQueryResultKey();
    return retval.flatMapIterable(d -> getNestedDocumentList(key, d))
                 .map(d -> d.toBsonDocument(Document.class, CodecRegistries.fromCodecs(new DocumentCodec())))
                 .map((d) -> deserialize(outputClass, d))
                 .doOnError(e -> LOGGER.error("Exception extracting results from document for key {}/output class {}",
                                              key, outputClass, e));
  }

  @SuppressWarnings("unchecked")
  private Iterable<? extends Document> getNestedDocumentList(String key, Document d) {
    Object object = getDocumentForKey(key, d, true);
    if (Iterable.class.isAssignableFrom(object.getClass())) {
      return (Iterable<Document>) object;
    }
    else {
      return Collections.singletonList((Document) object);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T deserialize(Class<T> outputClass, BsonDocument d) {
    // user wants to deserialize to a new class.
    // this registers the codec.
    ClassModel<T> classModel = ClassModel.builder(outputClass).build();
    PojoCodecProvider.Builder builder = PojoCodecProvider.builder().register(classModel).automatic(true);
    // ObjectIdAsString needs to be the first one so that we can use the ObjectIdAsString codec
    // to deserialize @Id fields as strings.
    CodecRegistry codecRegistry = fromProviders(new ObjectIdAsStringCodecProvider(),
                                                new BsonValueCodecProvider(),
                                                new ValueCodecProvider(),
                                                new IterableCodecProvider(),
                                                new UuidCodecProvider(UuidRepresentation.JAVA_LEGACY),
                                                new DocumentCodecProvider(),
                                                builder.build());
    Codec codec = codecRegistry.get(outputClass);
    OutputBuffer encoded = encode(DOCUMENT_CODEC, d);
    return (T) decode(codec, encoded);
  }

  @SuppressWarnings("unchecked")
  private <T> T decode(Codec codec, OutputBuffer buffer) {
    BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(
        ByteBuffer.wrap(buffer.toByteArray()))));
    return (T) codec.decode(reader, DecoderContext.builder().build());
  }

  private <T> OutputBuffer encode(final Codec<T> codec, final T value) {
    OutputBuffer buffer = new BasicOutputBuffer();
    BsonWriter writer = new BsonBinaryWriter(buffer);
    codec.encode(writer, value, EncoderContext.builder().build());
    return buffer;
  }
}
