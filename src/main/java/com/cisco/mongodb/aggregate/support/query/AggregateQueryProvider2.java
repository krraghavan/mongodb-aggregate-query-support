/*
 *  Copyright (c) 2017 the original author or authors.
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

import com.cisco.mongodb.aggregate.support.annotation.AggregateMetaAnnotation;
import com.cisco.mongodb.aggregate.support.annotation.Conditional;
import com.cisco.mongodb.aggregate.support.annotation.Out;
import com.cisco.mongodb.aggregate.support.annotation.v2.*;
import com.cisco.mongodb.aggregate.support.pageable.PageableFacet;
import com.cisco.mongodb.aggregate.support.pageable.PageableProject;
import com.cisco.mongodb.aggregate.support.pageable.PageableUnwind;
import com.cisco.mongodb.aggregate.support.processor.DefaultPipelineStageQueryProcessorFactory;
import com.cisco.mongodb.aggregate.support.processor.ParameterPlaceholderReplacingContext;
import com.cisco.mongodb.aggregate.support.processor.PipelineStageQueryProcessor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParameterAccessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.cisco.mongodb.aggregate.support.annotation.Conditional.*;
import static com.cisco.mongodb.aggregate.support.utils.ArrayUtils.NULL_STRING;
import static java.lang.String.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by rkolliva
 * 4/1/17.
 */
@SuppressWarnings("squid:S1067")
class AggregateQueryProvider2 extends AbstractAggregateQueryProvider {

  private static final String COULD_NOT_DETERMINE_QUERY = "Could not determine query";

  private static final String COULD_NOT_DETERMINE_ORDER = "Could not determine order for annotation %s with query %s";

  private static final Logger LOGGER = getLogger(AggregateQueryProvider2.class);

  /**
   * A separate logger used for query purposes so that clients can only dump the queries that
   * are created by this provider
   */
  private static final Logger QUERY_LOGGER = getLogger("com.cisco.mongodb.aggregate.support.query.AggregateQueryProvider2.Query");

  private DefaultPipelineStageQueryProcessorFactory queryProcessorFactory;

  private Aggregate2 aggregateAnnotation;

  private Class outputClass;

  private String collectioName;

  AggregateQueryProvider2(Method method, MongoParameterAccessor mongoParameterAccessor,
                          ConvertingParameterAccessor convertingParameterAccessor) throws
                                                                                   InvalidAggregationQueryException {
    super(method, mongoParameterAccessor, convertingParameterAccessor);
    this.aggregateAnnotation = method.getAnnotation(Aggregate2.class);
    this.outputClass = aggregateAnnotation.outputBeanType();
    this.collectioName = deriveCollectionName(aggregateAnnotation.inputType());
  }

  @Override
  protected void initializeAnnotation(Method method) {
    this.aggregateAnnotation = method.getAnnotation(Aggregate2.class);
    this.outputClass = aggregateAnnotation.outputBeanType();
    this.collectioName = deriveCollectionName(aggregateAnnotation.inputType());
    // set queryProcessorFactory here - the base class calls createQuery which needs the factory.
    this.queryProcessorFactory = new DefaultPipelineStageQueryProcessorFactory();
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
  public String getQueryResultKey() {
    return aggregateAnnotation.resultKey();
  }

  @Override
  public boolean isAggregate2() {
    return true;
  }

  @Override
  public boolean isAllowDiskUse() {
    return aggregateAnnotation.isAllowDiskUse();
  }

  @Override
  public long getMaxTimeMS() {
    return aggregateAnnotation.maxTimeMS();
  }

  @Override
  void createAggregateQuery() throws InvalidAggregationQueryException {
    LOGGER.debug(">>>> createAggregateQuery:: Forming aggregation pipeline");
    Annotation[] annotations = method.getAnnotations();
    List<Annotation> unwoundAnnotations = unwindAnnotations(annotations);
    addPageableStages(unwoundAnnotations);

    int pipelineCount = unwoundAnnotations.size();
    String[] queries = new String[pipelineCount];
    for (Annotation annotation : unwoundAnnotations) {
      Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
      Conditional [] conditionals = (Conditional[]) attributes.get("condition");
      ConditionalMatchType conditionalMatchType = (ConditionalMatchType) attributes.get("conditionMatchType");
      ParameterPlaceholderReplacingContext context =
          new ParameterPlaceholderReplacingContext(this, method,
                                                   new AggregationStage(AggregationType.from(annotation), conditionals,
                                                       conditionalMatchType),
                                                   annotation, getQueryString);
      PipelineStageQueryProcessor queryProcessor = queryProcessorFactory.getQueryProcessor(context);
      String query = queryProcessor.getQuery(context);
      int index = queryProcessor.getOrder(context);
      Class<? extends Annotation> annotationType = annotation.annotationType();
      if (query != null && index >= 0 && !NULL_STRING.equals(query)) {
        if (!StringUtils.isEmpty(queries[index])) {
          LOGGER.warn("Two stages have the same order and the second one did not evaluate to a false condition");
        }
        queries[index] = query;
      }
      else if (query != null && index == -1 && annotationType == Out.class) {
        // out stage only - add to end.
        queries[pipelineCount - 1] = query;
      }
      else if(index == -1){
        throw new IllegalArgumentException(format(COULD_NOT_DETERMINE_ORDER, annotationType.getCanonicalName(), query));
      }
    }
    aggregateQueryPipeline = arrayUtils.packToList(queries);
    QUERY_LOGGER.debug("Aggregate pipeline for query {} after forming queries - {}", aggregateAnnotation.name(), aggregateQueryPipeline);
  }

  private void addPageableStages(List<Annotation> unwoundAnnotations) {
    // if this annotation is pageable - add a facet.
    if(mongoParameterAccessor.getPageable() != null) {
      Pageable pageable = mongoParameterAccessor.getPageable();
      Annotation pageableFacet = new PageableFacet(unwoundAnnotations.size(), pageable.getOffset(),
                                                   pageable.getPageSize());
      Annotation pageableUnwind = new PageableUnwind(unwoundAnnotations.size() + 1);
      Annotation pageableProject = new PageableProject(unwoundAnnotations.size() + 2);

      unwoundAnnotations.addAll(Arrays.asList(pageableFacet, pageableUnwind, pageableProject));
      int outStageIndex = getOutPipelineStageIndex(unwoundAnnotations);
      if(outStageIndex >= 0) {
        Annotation outAnnotation = unwoundAnnotations.remove(outStageIndex);
        unwoundAnnotations.add(outAnnotation);
      }
    }
  }

  private List<Annotation> unwindAnnotations(Annotation[] annotations) {
    LOGGER.trace(">>>> unwindAnnotations - unwinding annotations");
    List<Annotation> retval = new ArrayList<>();
    for (Annotation annotation : annotations) {
      if (isAggregationPipelineStage(annotation)) {
        retval.add(annotation);
      }
      else if (isAggregationPipelineStageContainer(annotation)) {
        Annotation[] containingAnnotation = getContainingAnnotation(annotation);
        if(ArrayUtils.isNotEmpty(containingAnnotation)) {
          retval.addAll(unwindAnnotations(containingAnnotation));
        }
      }
      else if(isAggregationMetaAnnotation(annotation)) {
        Annotation[] containingAnnotations = getAggregateAnnotationsInMetaAnnotation(annotation);
        if(ArrayUtils.isNotEmpty(containingAnnotations)) {
          retval.addAll(unwindAnnotations(containingAnnotations));
        }
      }
    }
    LOGGER.trace("<<<< unwindAnnotations - unwinding annotations");
    return retval;
  }

  private boolean isAggregationMetaAnnotation(Annotation annotation) {
    return annotation.annotationType().getAnnotation(AggregateMetaAnnotation.class) != null;
  }

  private Annotation[] getAggregateAnnotationsInMetaAnnotation(Annotation annotation) {
    return annotation.annotationType().getAnnotations();
  }

  private Annotation[] getContainingAnnotation(Annotation annotation) {
    if(isAggregationPipelineStageContainer(annotation)) {
      return (Annotation[]) AnnotationUtils.getValue(annotation);
    }
    return new Annotation[0];
  }

  @Override
  public String getQueryForStage(Annotation annotation) {
    if (isAggregationPipelineStage(annotation)) {
      try {
        Method method = annotation.getClass().getDeclaredMethod("query");
        return (String) method.invoke(annotation);
      }
      catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        throw new IllegalArgumentException(COULD_NOT_DETERMINE_QUERY, e);
      }
    }
    return null;
  }

  private int getOutPipelineStageIndex(List<Annotation> annotations) {
    int index = 0;
    for(Annotation annotation : annotations) {
      Class<? extends Annotation> annotationType = annotation.annotationType();
      if(annotationType == Out.class) {
        return index;
      }
      ++index;
    }
    return -1;
  }

  private boolean isAggregationPipelineStage(Annotation annotation) {
    Class<? extends Annotation> annotationType = annotation.annotationType();
    return annotationType == Match2.class ||
           annotationType == Project2.class ||
           annotationType == Bucket2.class ||
           annotationType == Facet2.class ||
           annotationType == FacetPipelineStage.class ||
           annotationType == AddFields2.class ||
           annotationType == BucketAuto.class ||
           annotationType == SortByCount.class ||
           annotationType == GraphLookup.class ||
           annotationType == Count2.class ||
           annotationType == Sort2.class ||
           annotationType == Limit2.class ||
           annotationType == Lookup2.class ||
           annotationType == ReplaceRoot2.class ||
           annotationType == Skip2.class ||
           annotationType == Unwind2.class ||
           annotationType == Group2.class ||
           annotationType == Out.class
        ;
  }

  private boolean isAggregationPipelineStageContainer(Annotation annotation) {
    Class<? extends Annotation> annotationType = annotation.annotationType();
    return annotationType == Matches.class ||
           annotationType == Projects.class ||
           annotationType == Buckets.class ||
           annotationType == Facets.class ||
           annotationType == AddFieldss.class ||
           annotationType == Counts.class ||
           annotationType == BucketAutos.class ||
           annotationType == SortByCounts.class ||
           annotationType == Limits.class ||
           annotationType == Lookups.class ||
           annotationType == ReplaceRoots.class ||
           annotationType == Skips.class ||
           annotationType == Sorts.class ||
           annotationType == Unwinds.class ||
           annotationType == Groups.class
        ;
  }
}
