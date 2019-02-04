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

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Conditional;
import com.github.krr.mongodb.aggregate.support.annotations.Conditional.ConditionalMatchType;
import com.github.krr.mongodb.aggregate.support.annotations.Out;
import com.github.krr.mongodb.aggregate.support.enums.AggregationType;
import com.github.krr.mongodb.aggregate.support.exceptions.InvalidAggregationQueryException;
import com.github.krr.mongodb.aggregate.support.pageable.PageableFacet;
import com.github.krr.mongodb.aggregate.support.pageable.PageableProject;
import com.github.krr.mongodb.aggregate.support.pageable.PageableUnwind;
import com.github.krr.mongodb.aggregate.support.processor.DefaultPipelineStageQueryProcessorFactory;
import com.github.krr.mongodb.aggregate.support.processor.ParameterPlaceholderReplacingContext;
import com.github.krr.mongodb.aggregate.support.processor.PipelineStageQueryProcessor;
import com.github.krr.mongodb.aggregate.support.utils.ArrayUtils;
import com.github.krr.mongodb.aggregate.support.utils.ReactiveProcessorUtils;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.bson.BSON;
import org.bson.internal.Base64;
import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParameterAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.krr.mongodb.aggregate.support.utils.ArrayUtils.NULL_STRING;
import static com.mongodb.util.JSON.serialize;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by rkolliva
 * 4/1/17.
 */
@SuppressWarnings({"squid:S1067", "Duplicates"})
public class AggregateQueryProvider extends AbstractAggregateQueryProvider<Pageable> {

  private static final String COULD_NOT_DETERMINE_ORDER = "Could not determine order for annotation %s with query %s";

  private static final Logger LOGGER = getLogger(AggregateQueryProvider.class);

  private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

  /**
   * A separate logger used for query purposes so that clients can only dump the queries that
   * are created by this provider
   */
  private static final Logger QUERY_LOGGER = getLogger("AggregateQueryProvider.Query");

  private final MongoParameterAccessor mongoParameterAccessor;

  private final ConvertingParameterAccessor convertingParameterAccessor;

  private DefaultPipelineStageQueryProcessorFactory queryProcessorFactory;

  private final String entityCollectionName;

  private final ArrayUtils arrayUtils = new ArrayUtils();

  private final ReactiveProcessorUtils processorUtils = new ReactiveProcessorUtils();

  private StandardEvaluationContext context = new StandardEvaluationContext();

  private ParameterBindingParser parameterBindingParser = new NonReactiveParameterBindingParser();

  private boolean isLimiting;


  @SuppressWarnings("WeakerAccess")
  public AggregateQueryProvider(Method method, MongoParameterAccessor mongoParameterAccessor,
                                ConvertingParameterAccessor convertingParameterAccessor,
                                String entityCollectionName) throws
                                                                                          InvalidAggregationQueryException {
    super(method);
    this.mongoParameterAccessor = mongoParameterAccessor;
    this.convertingParameterAccessor = convertingParameterAccessor;
    this.entityCollectionName = entityCollectionName;
    initializeAnnotation(method);
    createAggregateQuery();
  }

  private Expression detectExpression(String collectionName) {
    Expression expression = EXPRESSION_PARSER.parseExpression(collectionName, ParserContext.TEMPLATE_EXPRESSION);
    return expression instanceof LiteralExpression ? null : expression;
  }

  @SuppressWarnings({"ConstantConditions", "Duplicates"})
  @Override
  protected void initializeAnnotation(Method method) throws InvalidAggregationQueryException {
    this.aggregateAnnotation = method.getAnnotation(Aggregate.class);
    Class inputType = aggregateAnnotation.inputType();
    this.collectionName = deriveCollectionName(aggregateAnnotation.inputType(),
                                               entityCollectionName,
                                               (idx) -> mongoParameterAccessor.getValues()[idx].toString(),
                                               () -> {
                                                 Document documentAnnotation = AnnotationUtils.findAnnotation(inputType,
                                                                                                              Document.class);
                                                 return documentAnnotation != null ? documentAnnotation.collection() : null;
                                               },
                                               (s) -> {
                                                 Expression expression = detectExpression(s);
                                                 if (expression != null) {
                                                   return expression.getValue(context, String.class);
                                                 }
                                                 return s;
                                               });
    this.placeholderRepFn = (q) -> replacePlaceholders((String) q);
    // set queryProcessorFactory here - the base class calls createQuery which needs the factory.
    this.queryProcessorFactory = new DefaultPipelineStageQueryProcessorFactory();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void createAggregateQuery() {
    LOGGER.debug(">>>> createAggregateQuery:: Forming aggregation pipeline");
    Annotation[] annotations = method.getAnnotations();
    List<Annotation> unwoundAnnotations = unwindAnnotations(annotations, AnnotationUtils::getValue);
    addPageableStages(unwoundAnnotations);

    int pipelineCount = unwoundAnnotations.size();
    String[] queries = new String[pipelineCount];
    for (Annotation annotation : unwoundAnnotations) {
      Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
      Conditional[] conditionals = (Conditional[]) attributes.get("condition");
      ConditionalMatchType conditionalMatchType = (ConditionalMatchType) attributes.get("conditionMatchType");
      AggregationType aggregationType = AggregationType.from(annotation);
      if(aggregationType == AggregationType.LIMIT) {
        isLimiting = true;
      }
      ParameterPlaceholderReplacingContext context =
          new ParameterPlaceholderReplacingContext(this, method,
                                                   new Spring4AggregationStage(aggregationType, conditionals,
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
      else if (index == -1) {
        throw new IllegalArgumentException(format(COULD_NOT_DETERMINE_ORDER, annotationType.getCanonicalName(), query));
      }
    }
    aggregateQueryPipeline = arrayUtils.packToList(queries);
    QUERY_LOGGER.debug("Aggregate pipeline for query {} after forming queries - {}", aggregateAnnotation.name(),
                       aggregateQueryPipeline);
  }

  @Override
  public boolean isLimiting() {
    return isLimiting;
  }

  @Override
  public Pageable getPageable() {
    return mongoParameterAccessor.getPageable();
  }

  private void addPageableStages(List<Annotation> unwoundAnnotations) {
    // if this annotation is pageable - add a facet.
    if(isPageable()) {
      Pageable pageable = mongoParameterAccessor.getPageable();

      Annotation pageableFacet = new PageableFacet(unwoundAnnotations.size(),
                                                   (int) pageable.getOffset(),
                                                   pageable.getPageSize());
      Annotation pageableUnwind = new PageableUnwind(unwoundAnnotations.size() + 1);
      Annotation pageableProject = new PageableProject(unwoundAnnotations.size() + 2);

      unwoundAnnotations.addAll(Arrays.asList(pageableFacet, pageableUnwind, pageableProject));
      int outStageIndex = getOutPipelineStageIndex(unwoundAnnotations);
      if (outStageIndex >= 0) {
        Annotation outAnnotation = unwoundAnnotations.remove(outStageIndex);
        unwoundAnnotations.add(outAnnotation);
      }
    }
  }

  /**
   * Replaced the parameter place-holders with the actual parameter values from the given {@link ParameterBinding}s.
   *
   * @param query - the query string with placeholders
   * @return - the string with values replaced
   */
  @SuppressWarnings({"Duplicates", "WeakerAccess"})
  protected String replacePlaceholders(String query) {
    List<ParameterBinding> queryParameterBindings = parameterBindingParser.parseParameterBindingsFrom(query, JSON::parse);

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

  /**
   * Returns the serialized value to be used for the given {@link ParameterBinding}.
   *
   * @param accessor - the accessor
   * @param binding  - the binding
   * @return - the value of the parameter
   */
  @SuppressWarnings("Duplicates")
  private String getParameterValueForBinding(ConvertingParameterAccessor accessor, ParameterBinding binding) {

    Object value = accessor.getBindableValue(binding.getParameterIndex());

    if (value instanceof String && binding.isQuoted()) {
      return (String) value;
    }
    else if (value instanceof byte[]) {

      String base64representation = Base64.encode((byte[]) value);
      if (!binding.isQuoted()) {
        return "{ '$binary' : '" + base64representation + "', '$type' : " + BSON.B_GENERAL + "}";
      }

      return base64representation;
    }

    return serialize(value);
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean isPageable() {
    return mongoParameterAccessor.getPageable().isPaged();
  }

  @Override
  public AggregationStage createAggregationStage(AggregationType type, Conditional[] condition,
                                                 ConditionalMatchType conditionalMatchType) {
    return new Spring4AggregationStage(type, condition, conditionalMatchType);
  }

  private class Spring4AggregationStage extends AggregationStage {

    Spring4AggregationStage(AggregationType aggregationType,
                            Conditional[] conditions,
                            ConditionalMatchType conditionalMatchType) {
      super(aggregationType, conditions, conditionalMatchType);
    }

    @Override
    public boolean allowStage() {
      return processorUtils.allowStage(conditionalClasses, conditionalMatchType, method,
                                       mongoParameterAccessor, convertingParameterAccessor);
    }

    @Override
    public boolean allowStage(Conditional[] conditionals, ConditionalMatchType type) {
      return processorUtils.allowStage(conditionals, type, method,
                                       mongoParameterAccessor, convertingParameterAccessor);
    }
  }

  private class NonReactiveParameterBindingParser extends ParameterBindingParser  {

    @SuppressWarnings("Duplicates")
    @Override
    protected void bindDriverSpecificTypes(List<ParameterBinding> bindings, Object value) {
      if (value instanceof DBRef) {

        DBRef dbref = (DBRef) value;
        potentiallyAddBinding(dbref.getCollectionName(), bindings);
        potentiallyAddBinding(dbref.getId().toString(), bindings);

      }
      else if (value instanceof DBObject) {

        DBObject dbo = (DBObject) value;

        for (String field : dbo.keySet()) {
          // replace @ params on lhs
          collectParameterReferencesIntoBindings(bindings, field);
          // replace ? params on RHS
          collectParameterReferencesIntoBindings(bindings, dbo.get(field));
        }
      }
    }
  }
}
