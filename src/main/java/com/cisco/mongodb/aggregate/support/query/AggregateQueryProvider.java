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

import com.cisco.mongodb.aggregate.support.annotation.*;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParameterAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cisco.mongodb.aggregate.support.query.AggregateQueryProvider.AggregationType.*;

/**
 * Created by rkolliva on 10/21/2015.
 * A query provider allowing aggregate queries
 */
public class AggregateQueryProvider implements QueryProvider, Iterator<String> {

  private static final String EMPTY_PIPELINE_FOR_AGGREGATION = "Empty pipeline for aggregation";

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregateQueryProvider.class);
  private static final ParameterBindingParser PARSER = ParameterBindingParser.INSTANCE;
  private final Class outputClass;
  private final String collectioName;
  private final Iterator<String> queryIterator;
  private final Aggregate aggregateAnnotation;
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final MongoParameterAccessor mongoParameterAccessor;
  private final ConvertingParameterAccessor convertingParameterAccessor;
  private final Method method;
  private List<String> aggregateQueryPipeline;

  AggregateQueryProvider(Method method, MongoParameterAccessor mongoParameterAccessor,
                         ConvertingParameterAccessor convertingParameterAccessor) throws InvalidAggregationQueryException {
    this.aggregateAnnotation = method.getAnnotation(Aggregate.class);
    this.outputClass = aggregateAnnotation.outputBeanType();
    this.mongoParameterAccessor = mongoParameterAccessor;
    this.convertingParameterAccessor = convertingParameterAccessor;
    this.collectioName = deriveCollectionName(aggregateAnnotation.inputType());
    this.method = method;
    // set up the query pipeline
    createAggregateQuery();
    // create the iterator - this class decorates the iterator
    this.queryIterator = aggregateQueryPipeline.iterator();
  }

  private String deriveCollectionName(Class className) {

    Document documentAnnotation = AnnotationUtils.findAnnotation(className, Document.class);
    String collectionName;
    if (documentAnnotation != null) {
      collectionName = documentAnnotation.collection();
    }
    else {
      String simpleName = className.getSimpleName();
      collectionName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    Assert.notNull(collectionName);
    return collectionName;
  }

  @Override
  public String getQuery() {
    throw new UnsupportedOperationException("Must use iterator methods to get the query for each stage of the aggregation pipeline");
  }

  @Override
  public Class getOutputClass() {
    return outputClass;
  }

  @Override
  public String getCollectionName() {
    return collectioName;
  }

  @Override
  public boolean isIterable() {
    return true;
  }

  @Override
  public boolean returnCollection() {
    return Collection.class.isAssignableFrom(method.getReturnType());
  }

  @Override
  public String getQueryResultKey() {
    return aggregateAnnotation.resultKey();
  }

  @Override
  public Class getMethodReturnType() {
    return method.getReturnType();
  }

  @Override
  public boolean hasNext() {
    return queryIterator.hasNext();
  }

  @Override
  public String next() {
    return queryIterator.next();
  }

  @Override
  public void remove() {
    queryIterator.remove();
  }

  @Override
  public void forEachRemaining(Consumer<? super String> action) {
    queryIterator.forEachRemaining(action);
  }

  /**
   * Returns the serialized value to be used for the given {@link ParameterBinding}.
   *
   * @param accessor - the accessor
   * @param binding - the binding
   * @return - the value of the parameter
   */
  private String getParameterValueForBinding(ConvertingParameterAccessor accessor, ParameterBinding binding) {

    Object value = accessor.getBindableValue(binding.getParameterIndex());

    if (value instanceof String && binding.isQuoted()) {
      return (String) value;
    }

    return JSON.serialize(value);
  }

  /*
   * *********** AGGREGATION SUPPORTS ***************************
   */

  /**
   * Replaced the parameter place-holders with the actual parameter values from the given {@link ParameterBinding}s.
   *
   * @param query - the query string with placeholders
   * @return - the string with values replaced
   */
  private String replacePlaceholders(String query) {
    List<ParameterBinding> queryParameterBindings = PARSER.parseParameterBindingsFrom(query);

    if (queryParameterBindings.isEmpty()) {
      return query;
    }

    StringBuilder result = new StringBuilder(query);

    for (ParameterBinding binding : queryParameterBindings) {

      String parameter = binding.getParameter();
      int idx = result.indexOf(parameter);

      if (idx != -1) {
        result.replace(idx, idx + parameter.length(), getParameterValueForBinding(convertingParameterAccessor,
                                                                                  binding));
      }
    }
    LOGGER.debug("Query after replacing place holders - {}", result);
    return result.toString();
  }

  private void createAggregateQuery() throws InvalidAggregationQueryException {
    // create the pipeline.
    LOGGER.debug("Getting aggregate operations");
    int pipelineCount = 0;
    boolean outAnnotationPresent = false;
    final Function<Object[], Integer> aggregateCounter = objects -> {
      if (objects != null) {
        return objects.length;
      }
      else {
        return 0;
      }
    };

    final BiFunction<AggregationType, String, String> getQueryString = (aggregationType, query) ->
        String.format("{%s:%s}", aggregationType.getRepresentation(), replacePlaceholders(query));
    LOGGER.debug("Extracting aggregate tests values");
    Project[] projections = aggregateAnnotation.project();
    Group[] groups = aggregateAnnotation.group();
    Unwind[] unwinds = aggregateAnnotation.unwind();
    Match[] matches = aggregateAnnotation.match();
    Lookup[] lookups = aggregateAnnotation.lookup();
    Limit[] limits = aggregateAnnotation.limit();
    Bucket [] buckets = aggregateAnnotation.bucket();
    Out out = aggregateAnnotation.out();
    pipelineCount += aggregateCounter.apply(projections);
    pipelineCount += aggregateCounter.apply(groups);
    pipelineCount += aggregateCounter.apply(unwinds);
    pipelineCount += aggregateCounter.apply(matches);
    pipelineCount += aggregateCounter.apply(lookups);
    pipelineCount += aggregateCounter.apply(limits);
    pipelineCount += aggregateCounter.apply(buckets);

    //If query is empty string then out was not declared in tests
    if (!"".equals(out.query())) {
      outAnnotationPresent = true;
      pipelineCount += 1;
    }
    if (pipelineCount == 0) {
      LOGGER.error(EMPTY_PIPELINE_FOR_AGGREGATION);
      throw new InvalidAggregationQueryException(EMPTY_PIPELINE_FOR_AGGREGATION);
    }
    else {
      String[] queries = new String[pipelineCount];
      for (Project projection : projections) {
        Assert.isTrue(projection.order() < pipelineCount, "Projection Order must be less than size");
        queries[projection.order()] = getQueryString.apply(PROJECT, projection.query());
      }
      for (Unwind unwind : unwinds) {
        Assert.isTrue(unwind.order() < pipelineCount, "Unwind Order must be less than size");
        queries[unwind.order()] = getQueryString.apply(UNWIND, unwind.query());
      }
      for (Group group : groups) {
        Assert.isTrue(group.order() < pipelineCount, "Group Order must be less than size");
        queries[group.order()] = getQueryString.apply(GROUP, group.query());
      }
      for (Match match : matches) {
        Assert.isTrue(match.order() < pipelineCount, "Match Order must be less than size");
        queries[match.order()] = getQueryString.apply(MATCH, match.query());
      }
      for (Lookup lookup : lookups) {
        Assert.isTrue(lookup.order() < pipelineCount, "Lookup Order " + lookup.order() + "must be less than "
                                                      + pipelineCount);
        queries[lookup.order()] = getQueryString.apply(LOOKUP, lookup.query());
      }
      for (Limit limit : limits) {
        Assert.isTrue(limit.order() < pipelineCount, "Limit Order " + limit.order() + "must be less than "
                                                     + pipelineCount);
        queries[limit.order()] = getQueryString.apply(LIMIT, limit.query());
      }
      for (Bucket bucket: buckets) {
        Assert.isTrue(bucket.order() < pipelineCount, "Bucket Order " + bucket.order() + " must be less than "
                                                     + pipelineCount);
        queries[bucket.order()] = getQueryString.apply(BUCKET, bucket.query());
      }
      //since only one out is allowed, place it at the end
      if (outAnnotationPresent) {
        queries[pipelineCount - 1] = getQueryString.apply(OUT, out.query());
      }

      //noinspection ConfusingArgumentToVarargsMethod
      LOGGER.debug("Aggregate pipeline after forming queries - {}", queries);

      aggregateQueryPipeline = Arrays.asList(queries);
    }
  }

  public enum AggregationType {
    MATCH("$match"),
    GROUP("$group"),
    UNWIND("$unwind"),
    LOOKUP("$lookup"),
    PROJECT("$project"),
    LIMIT("$limit"),
    BUCKET("$bucket"),
    OUT("$out");

    private final String representation;

    AggregationType(String representation) {
      this.representation = representation;
    }

    String getRepresentation() {
      return representation;
    }
  }

  /**
   * A parser that extracts the parameter bindings from a given query string.
   */
  private enum ParameterBindingParser {

    INSTANCE;

    private static final String PARAMETER_PREFIX = "_param_";
    private static final String PARSEABLE_PARAMETER = "\"" + PARAMETER_PREFIX + "$1\"";
    private static final Pattern PARAMETER_BINDING_PATTERN = Pattern.compile("\\?(\\d+)");
    private static final Pattern PARSEABLE_BINDING_PATTERN = Pattern.compile("\"?" + PARAMETER_PREFIX + "(\\d+)\"?");

    private static final int PARAMETER_INDEX_GROUP = 1;

    /**
     * Returns a list of {@link ParameterBinding}s found in the given {@code input} or an
     * {@link Collections#emptyList()}.
     *
     * @param input - the string with parameter bindings
     * @return - the list of parameters
     */
    public List<ParameterBinding> parseParameterBindingsFrom(String input) {

      if (!StringUtils.hasText(input)) {
        return Collections.emptyList();
      }

      List<ParameterBinding> bindings = new ArrayList<>();

      String parseableInput = makeParameterReferencesParseable(input);

      collectParameterReferencesIntoBindings(bindings, JSON.parse(parseableInput));

      return bindings;
    }

    private String makeParameterReferencesParseable(String input) {
      Matcher matcher = PARAMETER_BINDING_PATTERN.matcher(input);
      return matcher.replaceAll(PARSEABLE_PARAMETER);
    }

    private void collectParameterReferencesIntoBindings(List<ParameterBinding> bindings, Object value) {

      if (value instanceof String) {

        String string = ((String) value).trim();
        potentiallyAddBinding(string, bindings);

      }
      else if (value instanceof Pattern) {

        String string = value.toString().trim();

        Matcher valueMatcher = PARSEABLE_BINDING_PATTERN.matcher(string);
        while (valueMatcher.find()) {
          int paramIndex = Integer.parseInt(valueMatcher.group(PARAMETER_INDEX_GROUP));

                    /*
                     * The pattern is used as a direct parameter replacement, e.g. 'field': ?1,
                     * therefore we treat it as not quoted to remain backwards compatible.
                     */
          boolean quoted = !string.equals(PARAMETER_PREFIX + paramIndex);

          bindings.add(new ParameterBinding(paramIndex, quoted));
        }

      }
      else if (value instanceof DBRef) {

        DBRef dbref = (DBRef) value;

        potentiallyAddBinding(dbref.getCollectionName(), bindings);
        potentiallyAddBinding(dbref.getId().toString(), bindings);

      }
      else if (value instanceof DBObject) {

        DBObject dbo = (DBObject) value;

        for (String field : dbo.keySet()) {
          collectParameterReferencesIntoBindings(bindings, field);
          collectParameterReferencesIntoBindings(bindings, dbo.get(field));
        }
      }
    }

    private void potentiallyAddBinding(String source, List<ParameterBinding> bindings) {

      Matcher valueMatcher = PARSEABLE_BINDING_PATTERN.matcher(source);

      while (valueMatcher.find()) {

        int paramIndex = Integer.parseInt(valueMatcher.group(PARAMETER_INDEX_GROUP));
        boolean quoted = (source.startsWith("'") && source.endsWith("'"))
                         || (source.startsWith("\"") && source.endsWith("\""));

        bindings.add(new ParameterBinding(paramIndex, quoted));
      }
    }
  }

  /**
   * A generic parameter binding with name or position information.
   */
  static class ParameterBinding {

    private final int parameterIndex;
    private final boolean quoted;

    /**
     * Creates a new {@link ParameterBinding} with the given {@code parameterIndex} and {@code quoted} information.
     *
     * @param parameterIndex - the index of the parameterIndex
     * @param quoted         whether or not the parameter is already quoted.
     */
    ParameterBinding(int parameterIndex, boolean quoted) {

      this.parameterIndex = parameterIndex;
      this.quoted = quoted;
    }

    boolean isQuoted() {
      return quoted;
    }

    int getParameterIndex() {
      return parameterIndex;
    }

    String getParameter() {
      return "?" + parameterIndex;
    }
  }
}
