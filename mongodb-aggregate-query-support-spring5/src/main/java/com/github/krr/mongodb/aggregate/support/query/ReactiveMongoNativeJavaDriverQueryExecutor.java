package com.github.krr.mongodb.aggregate.support.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.mongodb.reactivestreams.client.AggregatePublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by rkolliva
 * 4/16/18.
 */

@SuppressWarnings({"unused", "rawtypes"})
public class ReactiveMongoNativeJavaDriverQueryExecutor extends ReactiveAbstractQueryExecutor<ReactiveMongoOperations> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveMongoNativeJavaDriverQueryExecutor.class);

  private static final String MONGO_V3_6_VERSION = "3.6";

  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private boolean isMongo360OrLater;

  public ReactiveMongoNativeJavaDriverQueryExecutor(ReactiveMongoOperations mongoOperations) {
    super(mongoOperations);
    initialize(mongoOperations);
  }

  public ReactiveMongoNativeJavaDriverQueryExecutor(ReactiveMongoOperations mongoOperations, ObjectMapper objectMapper) {
    super(mongoOperations, objectMapper);
    initialize(mongoOperations);
  }

  private void initialize(ReactiveMongoOperations mongoOperations) {
    Document result = mongoOperations.executeCommand("{buildinfo:1}").block();
    if (result == null) {
      throw new IllegalArgumentException("Could not check mongo version");
    }
    this.isMongo360OrLater = ((String) result.get("version")).startsWith(MONGO_V3_6_VERSION);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object executeQuery(QueryProvider queryProvider) {

    // convert the pipelines by parsing the JSON strings
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
    Mono<MongoCollection<Document>> collection = mongoOperations.getCollection(collectionName);
    Mono<AggregatePublisher<Document>> aggregatePublisherMono = collection.map(c -> c.aggregate(pipelineStages)
                                                                                     .allowDiskUse(queryProvider.isAllowDiskUse())
                                                                                     .maxTime(queryProvider.getMaxTimeMS(), MILLISECONDS));
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
      Function<AggregatePublisher<Document>, Flux<Document>> docsFlux = Flux::from;
      if (outputClass != null) {
        return aggregatePublisherMono.map(docsFlux)
                                     .map(f -> adaptPipeline(queryProvider, outputClass, f)).block();
      }
      return aggregatePublisherMono.map(docsFlux).block();
    }
    else {
      if (outputClass != null) {
        LOGGER.trace("Return type is Mono<{}>", outputClass);
        return aggregatePublisherMono.map(Mono::from)
                                     .map(f -> adaptPipeline(queryProvider, outputClass, f)).block();
      }
      return aggregatePublisherMono.map(Mono::from).block();
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

  private <T> T deserialize(Class<T> outputClass, BsonDocument d) {
    try {
      return objectMapper.readValue(d.toJson(), outputClass);
    }
    catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
