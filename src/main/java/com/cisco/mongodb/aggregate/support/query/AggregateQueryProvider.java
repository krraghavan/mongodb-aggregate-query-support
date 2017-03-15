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
import com.cisco.mongodb.aggregate.support.condition.AggregateQueryMethodConditionContext;
import com.cisco.mongodb.aggregate.support.condition.ConditionalAnnotationMetadata;
import com.cisco.mongodb.aggregate.support.utils.ArrayUtils;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
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
import static com.cisco.mongodb.aggregate.support.utils.ArrayUtils.NULL_STRING;

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
  private ArrayUtils arrayUtils = new ArrayUtils();

  private final BiFunction<AggregationStage, String, String> getQueryString = (aggregationStage, query) -> {
    if (aggregationStage.allowStage()) {
      String queryStringForStage = replacePlaceholders(query);
      if (!StringUtils.isEmpty(queryStringForStage)) {
        return String.format("{%s:%s}", aggregationStage.getAggregationType().getRepresentation(),
                             queryStringForStage);
      }
    }
    return NULL_STRING;
  };


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
      if(StringUtils.isEmpty(collectionName)) {
        collectionName = getSimpleCollectionName(className);
      }
    }
    else {
      collectionName = getSimpleCollectionName(className);
    }

    Assert.notNull(collectionName);
    return collectionName;
  }

  private String getSimpleCollectionName(Class className) {
    String collectionName;
    String simpleName = className.getSimpleName();
    collectionName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
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
    String lquery = query;
    if(query.contains("@@")) {
      // strip quotes from the query
      lquery = query.replace("\"", "").replace("@@", "@");
    }
    StringBuilder result = new StringBuilder(lquery);

    for (ParameterBinding binding : queryParameterBindings) {
      String parameter = binding.getParameter();
      int idx = result.indexOf(parameter);
      String parameterValueForBinding = getParameterValueForBinding(convertingParameterAccessor, binding);
      if (idx != -1) {
        result.replace(idx, idx + parameter.length(), parameterValueForBinding);
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
    final Function<Object, Integer> aggregateCounter = objects -> {
      if (objects != null) {
        if(objects instanceof Object []) {
          return ((Object[])objects).length;
        }
        else {
          return 1;
        }
      }
      else {
        return 0;
      }
    };

    LOGGER.debug("Extracting aggregate tests values");
    Project[] projections = aggregateAnnotation.project();
    Group[] groups = aggregateAnnotation.group();
    Unwind[] unwinds = aggregateAnnotation.unwind();
    Match[] matches = aggregateAnnotation.match();
    Lookup[] lookups = aggregateAnnotation.lookup();
    Limit[] limits = aggregateAnnotation.limit();
    Bucket [] buckets = aggregateAnnotation.bucket();
    Out out = aggregateAnnotation.out();
    AddFields[] addFields = aggregateAnnotation.addFields();
    ReplaceRoot[] replaceRoots = aggregateAnnotation.replaceRoot();
    Sort[] sorts = aggregateAnnotation.sort();
    Facet[] facets = aggregateAnnotation.facet();
    Count [] counts = aggregateAnnotation.count();
    Skip [] skips = aggregateAnnotation.skip();

    pipelineCount += aggregateCounter.apply(projections);
    pipelineCount += aggregateCounter.apply(groups);
    pipelineCount += aggregateCounter.apply(unwinds);
    pipelineCount += aggregateCounter.apply(matches);
    pipelineCount += aggregateCounter.apply(lookups);
    pipelineCount += aggregateCounter.apply(limits);
    pipelineCount += aggregateCounter.apply(buckets);
    pipelineCount += aggregateCounter.apply(addFields);
    pipelineCount += aggregateCounter.apply(replaceRoots);
    pipelineCount += aggregateCounter.apply(sorts);
    pipelineCount += aggregateCounter.apply(facets);
    pipelineCount += aggregateCounter.apply(counts);
    pipelineCount += aggregateCounter.apply(skips);

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
      addPipelineStages(queries, projections);
      addPipelineStages(queries, unwinds);
      addPipelineStages(queries, groups);
      addPipelineStages(queries, matches);
      addPipelineStages(queries, lookups);
      addPipelineStages(queries, limits);
      addPipelineStages(queries, buckets);
      addPipelineStages(queries, addFields);
      addPipelineStages(queries, replaceRoots);
      addPipelineStages(queries, sorts);
      addPipelineStages(queries, facets);
      addPipelineStages(queries, counts);
      addPipelineStages(queries, skips);

      //since only one out is allowed, place it at the end
      if (outAnnotationPresent) {
        setupQuery(queries, OUT, out.condition(), pipelineCount-1, out.query());
      }

      //noinspection ConfusingArgumentToVarargsMethod
      LOGGER.debug("Aggregate pipeline after forming queries - {}", (String[])queries);
      aggregateQueryPipeline = arrayUtils.packToList(queries);
    }
  }

  private void addPipelineStages(String[] queries, Group [] groups) {
    int length = queries.length;
    for (Group group : groups) {
      Assert.isTrue(group.order() < length, "Group Order must be less than " + length);
      setupQuery(queries, GROUP, group.condition(), group.order(), group.query());
    }
  }

  private void addPipelineStages(String[] queries, Match [] matches) {
    int length = queries.length;
    for (Match match : matches) {
      Assert.isTrue(match.order() < length, "Match Order must be less than " + length);
      setupQuery(queries, MATCH, match.condition(), match.order(), match.query());
    }
  }

  private void addPipelineStages(String[] queries, Lookup [] lookups) {
    int length = queries.length;
    for (Lookup lookup : lookups) {
      Assert.isTrue(lookup.order() < length, "Lookup Order must be less than " + length);
      setupQuery(queries, LOOKUP, lookup.condition(), lookup.order(), lookup.query());
    }
  }

  private void addPipelineStages(String[] queries, Limit [] limits) {
    int length = queries.length;
    for (Limit limit : limits) {
      Assert.isTrue(limit.order() < length, "Limit Order must be less than " + length);
      setupQuery(queries, LIMIT, limit.condition(), limit.order(), limit.query());
    }
  }

  private void addPipelineStages(String[] queries, Bucket [] buckets) {
    int length = queries.length;
    for (Bucket bucket : buckets) {
      Assert.isTrue(bucket.order() < length, "Bucket Order must be less than " + length);
      setupQuery(queries, BUCKET, bucket.condition(), bucket.order(), bucket.query());
    }
  }

  private void addPipelineStages(String[] queries, AddFields [] addFieldss) {
    int length = queries.length;
    for (AddFields addFields : addFieldss) {
      Assert.isTrue(addFields.order() < length, "AddFields Order must be less than " + length);
      setupQuery(queries, ADDFIELDS, addFields.condition(), addFields.order(), addFields.query());
    }
  }

  private void addPipelineStages(String[] queries, ReplaceRoot [] replaceRoots) {
    int length = queries.length;
    for (ReplaceRoot replaceRoot : replaceRoots) {
      Assert.isTrue(replaceRoot.order() < length, "ReplaceRoot Order must be less than " + length);
      setupQuery(queries, REPLACEROOT, replaceRoot.condition(), replaceRoot.order(), replaceRoot.query());
    }
  }

  private void addPipelineStages(String[] queries, Sort [] sorts) {
    int length = queries.length;
    for (Sort sort : sorts) {
      Assert.isTrue(sort.order() < length, "Sort Order must be less than " + length);
      setupQuery(queries, SORT, sort.condition(), sort.order(), sort.query());
    }
  }

  private void addPipelineStages(String[] queries, Facet [] facets) {
    int length = queries.length;
    for (Facet facet : facets) {
      Assert.isTrue(facet.order() < length, "Facet Order must be less than " + length);
      setupQuery(queries, FACET, facet.condition(), facet.order(), facet.query());
    }
  }

  private void addPipelineStages(String[] queries, Count [] counts) {
    int length = queries.length;
    for (Count count : counts) {
      Assert.isTrue(count.order() < length, "Count Order must be less than " + length);
      setupQuery(queries, COUNT, count.condition(), count.order(), count.query());
    }
  }

  private void addPipelineStages(String[] queries, Project[] projections) {
    int length = queries.length;
    for (Project projection : projections) {
      Assert.isTrue(projection.order() < length, "Projection Order must be less than " + length);
      setupQuery(queries, PROJECT, projection.condition(), projection.order(), projection.query());
    }
  }

  private void addPipelineStages(String[] queries, Unwind[] unwinds) {
    int length = queries.length;
    for (Unwind unwind : unwinds) {
      Assert.isTrue(unwind.order() < length, "Unwind Order must be less than " + length);
      setupQuery(queries, UNWIND, unwind.condition(), unwind.order(), unwind.query());
    }
  }

  private void addPipelineStages(String[] queries, Skip[] skips) {
    int length = queries.length;
    for (Skip skip : skips) {
      Assert.isTrue(skip.order() < length, "Skip Order must be less than " + length);
      setupQuery(queries, SKIP, skip.condition(), skip.order(), skip.query());
    }
  }

  private void setupQuery(String[] queries, AggregationType aggType, Conditional[] conditional, int order, String query) {
    AggregationStage stage = new AggregationStage(aggType, conditional);
    String queryString = getQueryString.apply(stage, query);
    if(!StringUtils.isEmpty(queries[order]) && !NULL_STRING.equals(queryString)) {
      // this stage is not empty - replace contents only if the query string is not null
      LOGGER.warn("Two stages have the same order and the second one did not evaluate to a false condition");
      queries[order] = queryString;
    }
    else if(StringUtils.isEmpty(queries[order])) {
      // replace this stage only if the query string is not the null string.
      queries[order] = queryString;
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

    private static final String LHS_PARAMETER_PREFIX = "@lhs@";
    private static final String LHS_PARSEABLE_PARAMETER = LHS_PARAMETER_PREFIX + "$1";
    private static final Pattern LHS_PARAMETER_BINDING_PATTERN = Pattern.compile("@(\\d+)");
    private static final Pattern LHS_PARSEABLE_BINDING_PATTERN = Pattern.compile(LHS_PARAMETER_PREFIX + "(\\d+)?");

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
      try {
        collectParameterReferencesIntoBindings(bindings, JSON.parse(parseableInput));
      }
      catch(JSONParseException e) {
        // the parseable input is not JSON - some stages like $unwind and $count only have strings.
        // nothing to do here.
        LOGGER.debug("JSONParseException:", e);
      }
      return bindings;
    }

    private String makeParameterReferencesParseable(String input) {
      Matcher matcher = PARAMETER_BINDING_PATTERN.matcher(input);
      String retval = matcher.replaceAll(PARSEABLE_PARAMETER);

      // now parse any LHS placeholders
      Matcher lhsMatcher = LHS_PARAMETER_BINDING_PATTERN.matcher(retval);
      return lhsMatcher.replaceAll(LHS_PARSEABLE_PARAMETER);
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

      boolean quoted = (source.startsWith("'") && source.endsWith("'"))
                       || (source.startsWith("\"") && source.endsWith("\""));
      replaceParameterBindings(bindings, valueMatcher, "?", quoted);
      Matcher lhsMatcher = LHS_PARSEABLE_BINDING_PATTERN.matcher(source);
      replaceParameterBindings(bindings, lhsMatcher, "@", true);
    }

    private void replaceParameterBindings(List<ParameterBinding> bindings, Matcher valueMatcher, String prefix,
                                          boolean quoted) {
      while (valueMatcher.find()) {

        int paramIndex = Integer.parseInt(valueMatcher.group(PARAMETER_INDEX_GROUP));

        bindings.add(new ParameterBinding(paramIndex, quoted, prefix));
      }
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
    ADDFIELDS("$addFields"),
    REPLACEROOT("$replaceRoot"),
    SORT("$sort"),
    FACET("$facet"),
    COUNT("$count"),
    SKIP("$skip"),
    OUT("$out");

    private final String representation;

    AggregationType(String representation) {
      this.representation = representation;
    }

    String getRepresentation() {
      return representation;
    }
  }

  private class AggregationStage {

    private final AggregationType aggregationType;

    private final Conditional[] conditionalClasses;

    AggregationStage(AggregationType aggregationType, Conditional[] conditionClass) {
      this.aggregationType = aggregationType;
      this.conditionalClasses = conditionClass;
    }

    AggregationStage(AggregationType aggregationType) {
      this(aggregationType, null);
    }

    AggregationType getAggregationType() {
      return aggregationType;
    }

    boolean allowStage() {
      if (conditionalClasses.length == 0) {
        return true;
      }
      try {
        for (Conditional conditional : conditionalClasses) {
          List<Object> parameterValues = getParameterValues();
          ConditionalAnnotationMetadata metadata = new ConditionalAnnotationMetadata(conditional);
          AggregateQueryMethodConditionContext context = new AggregateQueryMethodConditionContext(method,
                                                                                                  parameterValues);
          Condition condition = conditional.condition().newInstance();
          boolean isTrue = condition.matches(context, metadata);
          if (isTrue) {
            return true;
          }
        }
      }
      catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalStateException("Could not create an instance of the condition class", e);
      }
      return false;
    }

    private List<Object> getParameterValues() {
      List<Object> retval = new ArrayList<>();
      int numArgs = method.getParameterCount();
      for (int i = 0; i < numArgs; i++) {
        retval.add(convertingParameterAccessor.getBindableValue(i));
      }
      return retval;
    }

    public Conditional[] getConditionalClasses() {
      return conditionalClasses;
    }
  }

  /**
   * A generic parameter binding with name or position information.
   */
  static class ParameterBinding {

    private final int parameterIndex;
    private final boolean quoted;

    private final String prefix;

    /**
     * Creates a new {@link ParameterBinding} with the given {@code parameterIndex} and {@code quoted} information.
     *
     * @param parameterIndex - the index of the parameterIndex
     * @param quoted         whether or not the parameter is already quoted.
     */
    ParameterBinding(int parameterIndex, boolean quoted) {
      this(parameterIndex, quoted, "?");
    }

    ParameterBinding(int parameterIndex, boolean quoted, String prefix) {

      this.parameterIndex = parameterIndex;
      this.quoted = quoted;
      this.prefix = prefix;
    }

    boolean isQuoted() {
      return quoted;
    }

    int getParameterIndex() {
      return parameterIndex;
    }

    String getParameter() {
      return this.prefix + parameterIndex;
    }
  }
}
