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

package com.github.krr.mongodb.aggregate.support.query;

import com.github.krr.mongodb.aggregate.support.annotation.*;
import com.github.krr.mongodb.aggregate.support.pageable.PageableFacet;
import com.github.krr.mongodb.aggregate.support.pageable.PageableProject;
import com.github.krr.mongodb.aggregate.support.pageable.PageableUnwind;
import com.github.krr.mongodb.aggregate.support.processor.DefaultPipelineStageQueryProcessorFactory;
import com.github.krr.mongodb.aggregate.support.processor.ParameterPlaceholderReplacingContext;
import com.github.krr.mongodb.aggregate.support.processor.PipelineStageQueryProcessor;
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

import static java.lang.String.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by rkolliva
 * 4/1/17.
 */
@SuppressWarnings("squid:S1067")
class AggregateQueryProvider extends AbstractAggregateQueryProvider {

  private static final String COULD_NOT_DETERMINE_QUERY = "Could not determine query";

  private static final String COULD_NOT_DETERMINE_ORDER = "Could not determine order for annotation %s with query %s";

  private static final Logger LOGGER = getLogger(AggregateQueryProvider.class);

  /**
   * A separate logger used for query purposes so that clients can only dump the queries that
   * are created by this provider
   */
  private static final Logger QUERY_LOGGER = getLogger("com.cisco.mongodb.aggregate.support.query.AggregateQueryProvider2.Query");

  private DefaultPipelineStageQueryProcessorFactory queryProcessorFactory;

  private Aggregate aggregateAnnotation;

  private String collectioName;

  AggregateQueryProvider(Method method, MongoParameterAccessor mongoParameterAccessor,
                         ConvertingParameterAccessor convertingParameterAccessor) throws
                                                                                   InvalidAggregationQueryException {
    super(method, mongoParameterAccessor, convertingParameterAccessor);
    this.aggregateAnnotation = method.getAnnotation(Aggregate.class);
    this.collectioName = deriveCollectionName(aggregateAnnotation.inputType());
  }

  @Override
  protected String deriveCollectionName(Class className) throws InvalidAggregationQueryException {
    // if the aggregate query has a parameter annotated with @CollectionName use that
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    int parameterCount = method.getParameterCount();
    String retval = null;
    int collectionNameAnnotationCount = 0;
    List<Integer> invalidParamPositions = new ArrayList<>();
    for(int i = 0; i < parameterCount; i++) {
      // validate each parameter - we only allow @CollectionName to occur once.
      Annotation[] annotationsOnParameter = parameterAnnotations[i];
      int parameterAnnotationCount = annotationsOnParameter.length;
      if(parameterAnnotationCount > 0) {
        LOGGER.debug("Parameter at position {} has {} annotations", i, parameterAnnotationCount);
        for (Annotation parameterAnnotation : annotationsOnParameter) {
          if (parameterAnnotation.annotationType() == CollectionName.class) {
            retval = mongoParameterAccessor.getValues()[i].toString();
            collectionNameAnnotationCount++;
            invalidParamPositions.add(i);
          }
        }
      }
    }

    if(collectionNameAnnotationCount > 1) {
      String msg = String.format("Found more than one CollectionName annotation on parameters.  Found at positions:%s",
                                 StringUtils.join(invalidParamPositions, ","));
      LOGGER.error(msg);
      throw new InvalidAggregationQueryException(msg);

    }
    else if(collectionNameAnnotationCount == 0) {
      // no collection name annotation present.
      retval = super.deriveCollectionName(className);
    }
    return retval;
  }

  @Override
  protected void initializeAnnotation(Method method) throws InvalidAggregationQueryException {
    this.aggregateAnnotation = method.getAnnotation(Aggregate.class);
    this.collectioName = deriveCollectionName(aggregateAnnotation.inputType());
    // set queryProcessorFactory here - the base class calls createQuery which needs the factory.
    this.queryProcessorFactory = new DefaultPipelineStageQueryProcessorFactory();
  }

  @Override
  public Class getOutputClass() {
    return aggregateAnnotation.outputBeanType();
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
  public boolean isAllowDiskUse() {
    return aggregateAnnotation.isAllowDiskUse();
  }

  @Override
  public long getMaxTimeMS() {
    return aggregateAnnotation.maxTimeMS();
  }

  @Override
  void createAggregateQuery() {
    LOGGER.debug(">>>> createAggregateQuery:: Forming aggregation pipeline");
    Annotation[] annotations = method.getAnnotations();
    List<Annotation> unwoundAnnotations = unwindAnnotations(annotations);
    addPageableStages(unwoundAnnotations);

    int pipelineCount = unwoundAnnotations.size();
    String[] queries = new String[pipelineCount];
    for (Annotation annotation : unwoundAnnotations) {
      Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
      Conditional[] conditionals = (Conditional[]) attributes.get("condition");
      Conditional.ConditionalMatchType conditionalMatchType = (Conditional.ConditionalMatchType) attributes.get("conditionMatchType");
      ParameterPlaceholderReplacingContext context =
          new ParameterPlaceholderReplacingContext(this, method,
                                                   new AggregationStage(AggregationType.from(annotation), conditionals,
                                                       conditionalMatchType),
                                                   annotation, getQueryString);
      PipelineStageQueryProcessor queryProcessor = queryProcessorFactory.getQueryProcessor(context);
      String query = queryProcessor.getQuery(context);
      int index = queryProcessor.getOrder(context);
      Class<? extends Annotation> annotationType = annotation.annotationType();
      if (query != null && index >= 0 && !com.github.krr.mongodb.aggregate.support.utils.ArrayUtils.NULL_STRING.equals(query)) {
        if(index >= queries.length) {
          String msg = String.format("Index specified in query [%d] is larger than the number of pipeline stages [%d]",
                                     index, queries.length);
          LOGGER.error(msg);
          throw new IllegalArgumentException(msg);
        }
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
    Pageable pageableParam = mongoParameterAccessor.getPageable();
    if(pageableParam != Pageable.unpaged()) {
      Annotation pageableFacet = new PageableFacet(unwoundAnnotations.size(), (int) pageableParam.getOffset(),
                                                   pageableParam.getPageSize());
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
    return annotationType == Match.class ||
           annotationType == Project.class ||
           annotationType == Bucket.class ||
           annotationType == Facet.class ||
           annotationType == FacetPipelineStage.class ||
           annotationType == AddFields.class ||
           annotationType == BucketAuto.class ||
           annotationType == SortByCount.class ||
           annotationType == GraphLookup.class ||
           annotationType == Count.class ||
           annotationType == Sort.class ||
           annotationType == Limit.class ||
           annotationType == Lookup.class ||
           annotationType == ReplaceRoot.class ||
           annotationType == Skip.class ||
           annotationType == Unwind.class ||
           annotationType == Group.class ||
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
