package com.github.krr.mongodb.aggregate.support.query;


import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.github.krr.mongodb.aggregate.support.enums.AggregationType;
import com.github.krr.mongodb.aggregate.support.exceptions.InvalidAggregationQueryException;
import com.github.krr.mongodb.aggregate.support.utils.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.krr.mongodb.aggregate.support.utils.ArrayUtils.NULL_STRING;

/**
 * Created by rkolliva
 * 4/25/18.
 */

@SuppressWarnings("WeakerAccess")
public abstract class AbstractAggregateQueryProvider<T> implements QueryProvider<T> {

  protected final Method method;

  protected List<String> aggregateQueryPipeline;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAggregateQueryProvider.class);

  private static final String COULD_NOT_DETERMINE_QUERY = "Could not determine query";

  protected Function<String, String> placeholderRepFn;

  protected String collectionName;

  protected Aggregate aggregateAnnotation;

  protected AbstractAggregateQueryProvider(Method method) {
    this.method = method;
  }

  protected final BiFunction<AggregationStage, String, String> getQueryString = (aggregationStage, query) -> {
    if (aggregationStage.allowStage()) {
      String queryStringForStage = placeholderRepFn.apply(query);
      if (!StringUtils.isEmpty(queryStringForStage)) {
        return String.format("{%s:%s}", aggregationStage.getAggregationType().getRepresentation(),
                             queryStringForStage);
      }
    }
    return NULL_STRING;
  };
  protected abstract void initializeAnnotation(Method method) throws InvalidAggregationQueryException;

  protected abstract void createAggregateQuery() throws InvalidAggregationQueryException;

  /**
   * Derives the collection name on which the aggregate query will be run.
   *
   * @param aggregateAnnotation          - the aggregate annotation on the query
   *
   * @param documentAnnotationNameSupplier - a supplier for the document annotation.
   *                                       Modeled as a supplier to avoid Spring version
   *                                       specific dependencies in this class.
   *
   * @param parameterValueSupplier - a supplier that returns the collection name from the @{@link CollectionName}
   *                               annotated parameter
   * @param expressionBasedNameSupplier - a function that takes a String input and processes it for SpEl expressions
   *                                    if it contains any.
   * @return - the collection name
   * @throws IllegalArgumentException if the collection name is null at the end of
   *                                  the evaluation.
   * @throws InvalidAggregationQueryException - if the collection name could not be derived (e.g. more than
   * one parameter contains an @{@link CollectionName} annotation.
   */
  protected String deriveCollectionName(Aggregate aggregateAnnotation,
                                        Function<Integer, String> parameterValueSupplier,
                                        Supplier<String> documentAnnotationNameSupplier,
                                        Function<String, String> expressionBasedNameSupplier) throws
                                                                                      InvalidAggregationQueryException {
    LOGGER.trace(">>>> AbstractAggregateQueryProvider::deriveCollectionName for {}", aggregateAnnotation.inputType());

    String collectionName = aggregateAnnotation.collectionName();
    if(!StringUtils.isEmpty(collectionName.trim())) {
      return collectionName;
    }
    Class className = aggregateAnnotation.inputType();

    // if the @CollectionName annotation is present use that.
    collectionName = deriveCollectionName(parameterValueSupplier);
    if(!StringUtils.isEmpty(collectionName)) {
      LOGGER.debug("Returning collection name as {} from @CollectionName annotated parameter", className);
      return collectionName;
    }
    // check if document annotation is present on bean.
    collectionName = documentAnnotationNameSupplier.get();

    // if @Document annoation is present
    if (StringUtils.isEmpty(collectionName)) {
      // Not present - derive it from class name.
      collectionName = getSimpleCollectionName(className);
      LOGGER.debug("Returning collection name based on bean name {}", collectionName);
      return collectionName;
    }
    // @Document present but may be an expression.
    // if the collection name is an expression process it here.
    collectionName = expressionBasedNameSupplier.apply(collectionName);
    Assert.notNull(collectionName, "Collection name must not be null");
    LOGGER.trace("<<<< AbstractAggregateQueryProvider::deriveCollectionName: class{}, collectionName:{}",
                 className, collectionName);
    return collectionName;
  }

  private String deriveCollectionName(Function<Integer, String> parameterValueProvider)
      throws InvalidAggregationQueryException {

    // if the aggregate query has a parameter annotated with @CollectionName use that
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    int parameterCount = method.getParameterCount();
    return collectionNameFromAnnotation(parameterAnnotations, parameterCount, parameterValueProvider);
  }


  protected String collectionNameFromAnnotation(Annotation[][] parameterAnnotations,
                                                int parameterCount,
                                                Function<Integer, String> parameterValueFn) throws
                                                                                            InvalidAggregationQueryException {
    String retval = null;
    int collectionNameAnnotationCount = 0;
    List<Integer> invalidParamPositions = new ArrayList<>();
    for (int i = 0; i < parameterCount; i++) {
      // validate each parameter - we only allow @CollectionName to occur once.
      Annotation[] annotationsOnParameter = parameterAnnotations[i];
      int parameterAnnotationCount = annotationsOnParameter.length;
      if (parameterAnnotationCount > 0) {
        LOGGER.debug("Parameter at position {} has {} annotations", i, parameterAnnotationCount);
        for (Annotation parameterAnnotation : annotationsOnParameter) {
          if (parameterAnnotation.annotationType() == CollectionName.class) {
            retval = parameterValueFn.apply(i);
            collectionNameAnnotationCount++;
            invalidParamPositions.add(i);
          }
        }
      }
    }
    if (collectionNameAnnotationCount > 1) {
      String msg = String.format("Found more than one CollectionName annotation on parameters.  Found at positions:%s",
                                 StringUtils.join(invalidParamPositions, ","));
      LOGGER.error(msg);
      throw new InvalidAggregationQueryException(msg);
    }

    return retval;
  }

  protected boolean isAggregationMetaAnnotation(Annotation annotation) {
    return annotation.annotationType().getAnnotation(AggregateMetaAnnotation.class) != null;
  }

  protected Annotation[] getAggregateAnnotationsInMetaAnnotation(Annotation annotation) {
    return annotation.annotationType().getAnnotations();
  }

  protected Annotation[] getContainingAnnotation(Annotation annotation, Function<Annotation, Object> valueSupplier) {
    if (isAggregationPipelineStageContainer(annotation)) {
      return (Annotation[]) valueSupplier.apply(annotation);
    }
    return new Annotation[0];
  }

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

  protected int getOutPipelineStageIndex(List<Annotation> annotations) {
    int index = 0;
    for (Annotation annotation : annotations) {
      Class<? extends Annotation> annotationType = annotation.annotationType();
      // out and merge are terminal steps
      if (annotationType == Out.class || annotationType == Merge.class) {
        return index;
      }
      ++index;
    }
    return -1;
  }

  protected List<Annotation> unwindAnnotations(Annotation[] annotations, Function<Annotation, Object> valueSupplier) {
    LOGGER.trace(">>>> unwindAnnotations - unwinding annotations");
    List<Annotation> retval = new ArrayList<>();
    for (Annotation annotation : annotations) {
      if (isAggregationPipelineStage(annotation)) {
        retval.add(annotation);
      }
      else if (isAggregationPipelineStageContainer(annotation)) {
        Annotation[] containingAnnotation = getContainingAnnotation(annotation, valueSupplier);
        if (ArrayUtils.isNotEmpty(containingAnnotation)) {
          retval.addAll(unwindAnnotations(containingAnnotation, valueSupplier));
        }
      }
      else if (isAggregationMetaAnnotation(annotation)) {
        Annotation[] containingAnnotations = getAggregateAnnotationsInMetaAnnotation(annotation);
        if (ArrayUtils.isNotEmpty(containingAnnotations)) {
          retval.addAll(unwindAnnotations(containingAnnotations, valueSupplier));
        }
      }
    }
    LOGGER.trace("<<<< unwindAnnotations - unwinding annotations");
    return retval;
  }

  protected String getSimpleCollectionName(Class className) {
    String collectionName;
    String simpleName = className.getSimpleName();
    collectionName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    return collectionName;
  }

  protected boolean isAggregationPipelineStage(Annotation annotation) {
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
           annotationType == Out.class ||
           annotationType == Merge.class
        ;
  }

  protected boolean isAggregationPipelineStageContainer(Annotation annotation) {
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

  @Override
  public Class getOutputClass() {
    return aggregateAnnotation.outputBeanType();
  }

  @Override
  public String getCollectionName() {
    return collectionName;
  }

  @Override
  public Class getMethodReturnType() {
    return method.getReturnType();
  }

  @Override
  public String getQueryResultKey() {
    return aggregateAnnotation.resultKey();
  }

  public abstract T getPageable();

  @Override
  public boolean isAllowDiskUse() {
    return aggregateAnnotation.isAllowDiskUse();
  }

  @Override
  public long getMaxTimeMS() {
    return aggregateAnnotation.maxTimeMS();
  }

  @Override
  public List<String> modifyAggregateQueryPipeline(String newStage, int stage) {
    return null;
  }

  @Override
  public List<String> getPipelines() {
    if(CollectionUtils.isEmpty(aggregateQueryPipeline)) {
      throw new IllegalArgumentException("Empty query pipeline");
    }
    return aggregateQueryPipeline;
  }

  /**
   * A generic parameter binding with name or position information.
   */
  public static class ParameterBinding {

    private final int parameterIndex;

    /**
     * Indicates whether a string is already quoted (in the expression) or
     * if quoting should be avoided (for JSON string expressions)
     */
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

    public boolean stripQuotes() {
      return "@".equals(prefix);
    }

    public boolean isQuoted() {
      return quoted;
    }

    public int getParameterIndex() {
      return parameterIndex;
    }

    public String getParameter() {
      return this.prefix + parameterIndex;
    }
  }

  public abstract static class AggregationStage {

    protected final AggregationType aggregationType;

    protected final Conditional[] conditionalClasses;

    protected Conditional.ConditionalMatchType conditionalMatchType;

    public AggregationStage(AggregationType aggregationType, Conditional[] conditionClass,
                            Conditional.ConditionalMatchType conditionalMatchType) {
      this.aggregationType = aggregationType;
      this.conditionalClasses = conditionClass;
      this.conditionalMatchType = conditionalMatchType;
    }

    @SuppressWarnings("unused")
    public AggregationStage(AggregationType aggregationType) {
      this(aggregationType, new Conditional[0], Conditional.ConditionalMatchType.ANY);
    }

    AggregationType getAggregationType() {
      return aggregationType;
    }

    public abstract boolean allowStage();

    public abstract boolean allowStage(Conditional[] conditionals, Conditional.ConditionalMatchType type);
  }
}
