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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParameterAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Function;

import static com.cisco.mongodb.aggregate.support.query.AbstractAggregateQueryProvider.AggregationType.*;
import static com.cisco.mongodb.aggregate.support.utils.ArrayUtils.NULL_STRING;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by rkolliva on 10/21/2015.
 * A query provider allowing aggregate queries
 * @deprecated
 */
@SuppressWarnings("WeakerAccess")
@Deprecated
public class AggregateQueryProvider extends AbstractAggregateQueryProvider {

  private static final String EMPTY_PIPELINE_FOR_AGGREGATION = "Empty pipeline for aggregation";

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregateQueryProvider.class);

  /**
   * A separate logger used for query purposes so that clients can only dump the queries that
   * are created by this provider
   */
  private static final Logger QUERY_LOGGER = getLogger("com.cisco.mongodb.aggregate.support.query.AggregateQueryProvider.Query");

  private Class outputClass;
  private String collectioName;
  private Aggregate aggregateAnnotation;

  AggregateQueryProvider(Method method, MongoParameterAccessor mongoParameterAccessor,
                         ConvertingParameterAccessor convertingParameterAccessor) throws InvalidAggregationQueryException {
    super(method, mongoParameterAccessor, convertingParameterAccessor);
  }

  @Override
  protected void initializeAnnotation(Method method) {
    this.aggregateAnnotation = method.getAnnotation(Aggregate.class);
    this.outputClass = aggregateAnnotation.outputBeanType();
    this.collectioName = deriveCollectionName(aggregateAnnotation.inputType());
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
  public boolean isAggregate2() {
    return false;
  }

  @Override
  public Class getMethodReturnType() {
    return method.getReturnType();
  }

  @Override
  void createAggregateQuery() throws InvalidAggregationQueryException {
    // create the pipeline.
    LOGGER.debug("Getting aggregate operations");
    int pipelineCount = 0;
    boolean outAnnotationPresent = false;
    boolean isPageable = this.mongoParameterAccessor.getPageable() != null;
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
    if (isPageable) {
      pipelineCount += 2;
    }

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

      addToEndOfQuery(pipelineCount, outAnnotationPresent, isPageable, out, queries, mongoParameterAccessor.getPageable());

      //noinspection ConfusingArgumentToVarargsMethod
      QUERY_LOGGER.debug("Aggregate pipeline for query ({}) after forming queries - {}", aggregateAnnotation.name(), queries);
      aggregateQueryPipeline = arrayUtils.packToList(queries);
    }
  }

  private void addToEndOfQuery(int pipelineCount, boolean outAnnotationPresent, boolean isPageable, Out out,
                               String[] queries, Pageable pageable) {
    int lastStage = pipelineCount - 1;
    int skipStageForPageable = lastStage - 1;
    int limitStageForPageable = lastStage;
    LOGGER.debug("Last stage is {}", lastStage);
    //shift skipStageForPageable back one if client is performing an out
    if (outAnnotationPresent) {
      LOGGER.debug("Decrementing potential pageable stage since out annotation is present");
      skipStageForPageable--;
      limitStageForPageable--;
    }

    if (isPageable) {
      LOGGER.debug("isPageable is true, adding skip and limit stages");
      setupQuery(queries, AggregationType.SKIP, new Conditional[]{}, skipStageForPageable,
                 String.valueOf(pageable.getPageNumber() * pageable.getPageSize()));
      setupQuery(queries, LIMIT, new Conditional[]{}, limitStageForPageable, String.valueOf(pageable.getPageSize()));
    }

    //since only one out is allowed, place it at the end
    if (outAnnotationPresent) {
      LOGGER.debug("outAnnotation is present, adding to last stage");
      setupQuery(queries, OUT, out.condition(), lastStage, out.query());
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

  @Override
  public String getQueryForStage(Annotation annotation) {
    throw new UnsupportedOperationException("Not supported yet");
  }
}
