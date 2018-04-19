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
import com.github.krr.mongodb.aggregate.support.utils.ArrayUtils;
import com.github.krr.mongodb.aggregate.support.utils.ProcessorUtils;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import com.mongodb.util.JSONSerializers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rkolliva on 10/21/2015.
 * A query provider allowing aggregate queries
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractAggregateQueryProvider implements QueryProvider, Iterator<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAggregateQueryProvider.class);

  protected static final ParameterBindingParser PARSER = ParameterBindingParser.INSTANCE;

  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  protected final MongoParameterAccessor mongoParameterAccessor;
  protected final ConvertingParameterAccessor convertingParameterAccessor;
  protected final Method method;
  protected List<String> aggregateQueryPipeline;
  protected Iterator<String> queryIterator = null;
  protected ArrayUtils arrayUtils = new ArrayUtils();
  protected ProcessorUtils processorUtils = new ProcessorUtils();

  protected static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

  protected StandardEvaluationContext context = new StandardEvaluationContext();

  protected final BiFunction<AggregationStage, String, String> getQueryString = (aggregationStage, query) -> {
    if (aggregationStage.allowStage()) {
      String queryStringForStage = replacePlaceholders(query);
      if (!StringUtils.isEmpty(queryStringForStage)) {
        return String.format("{%s:%s}", aggregationStage.getAggregationType().getRepresentation(),
                             queryStringForStage);
      }
    }
    return ArrayUtils.NULL_STRING;
  };


  AbstractAggregateQueryProvider(Method method, MongoParameterAccessor mongoParameterAccessor,
                                 ConvertingParameterAccessor convertingParameterAccessor) throws InvalidAggregationQueryException {
    this.mongoParameterAccessor = mongoParameterAccessor;
    this.convertingParameterAccessor = convertingParameterAccessor;
    this.method = method;
    initializeAnnotation(method);
    // set up the query pipeline
    createAggregateQuery();
  }

  private void initializeIterator() {
    // create the iterator - this class decorates the iterator
    if (this.queryIterator == null) {
      this.queryIterator = aggregateQueryPipeline.iterator();
    }
  }
  protected abstract void initializeAnnotation(Method method) throws InvalidAggregationQueryException;

  public abstract String getQueryForStage(Annotation annotation);

  protected String deriveCollectionName(Class className) throws InvalidAggregationQueryException {

    Document documentAnnotation = AnnotationUtils.findAnnotation(className, Document.class);
    String collectionName;
    if (documentAnnotation != null) {
      collectionName = documentAnnotation.collection();
      if(StringUtils.isEmpty(collectionName)) {
        collectionName = getSimpleCollectionName(className);
      }
      else {
        Expression expression = detectExpression(collectionName);
        if(expression != null) {
          collectionName = expression.getValue(context, String.class);
        }
      }
    }
    else {
      collectionName = getSimpleCollectionName(className);
    }

    Assert.notNull(collectionName, "Collection name must not be null");
    return collectionName;
  }

  private Expression detectExpression(String collectionName) {
    Expression expression = EXPRESSION_PARSER.parseExpression(collectionName, ParserContext.TEMPLATE_EXPRESSION);
    return expression instanceof LiteralExpression ? null : expression;
  }

  protected String getSimpleCollectionName(Class className) {
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
  public boolean isIterable() {
    return true;
  }

  @Override
  public Class getMethodReturnType() {
    return method.getReturnType();
  }

  @Override
  public boolean hasNext() {
    initializeIterator();
    return queryIterator.hasNext();
  }

  @Override
  public String next() {
    initializeIterator();
    return queryIterator.next();
  }

  @Override
  public void remove() {
    initializeIterator();
    queryIterator.remove();
  }

  @Override
  public void forEachRemaining(Consumer<? super String> action) {
    initializeIterator();
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

    return serialize(value);
  }

  private static String serialize(Object object) {
    StringBuilder buf = new StringBuilder();
    JSONSerializers.getStrict().serialize(object, buf);
    return buf.toString();
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
  protected String replacePlaceholders(String query) {
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

  abstract void createAggregateQuery() throws InvalidAggregationQueryException;

  /**
   * A parser that extracts the parameter bindings from a given query string.
   */
  protected enum ParameterBindingParser {

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
        LOGGER.trace("JSONParseException:", e);
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
    MATCH2("$match", Match.class),
    GROUP2("$group", Group.class),
    UNWIND2("$unwind", Unwind.class),
    LOOKUP2("$lookup", Lookup.class),
    PROJECT2("$project", Project.class),
    LIMIT2("$limit", Limit.class),
    BUCKET2("$bucket", Bucket.class),
    ADDFIELDS2("$addFields", AddFields.class),
    REPLACEROOT2("$replaceRoot", ReplaceRoot.class),
    SORT2("$sort", Sort.class),
    SORTBYCOUNT("$sortByCount", SortByCount.class),
    BUCKETAUTO("$bucketAuto", BucketAuto.class),
    GRAPHLOOKUP("$graphLookup", GraphLookup.class),
    FACET2("$facet", Facet.class),
    COUNT2("$count", Count.class),
    SKIP2("$skip", Skip.class),
    OUT("$out", Out.class);

    private final String representation;

    private final Class<? extends Annotation> annotationClass;

    AggregationType(String representation, Class<? extends Annotation> annotationClass) {
      this.representation = representation;
      this.annotationClass = annotationClass;
    }

    public Class<? extends Annotation> getAnnotationClass() {
      return annotationClass;
    }

    String getRepresentation() {
      return representation;
    }

    public static AggregationType from(Annotation annotation) {
      return from(annotation.annotationType());
    }

    public static AggregationType from(Class<? extends Annotation> stageType) {
      for(AggregationType type : values()) {
        if(type.getAnnotationClass() == stageType) {
          return type;
        }
      }
      throw new IllegalArgumentException("Unknown annotation type " + stageType.getName());
    }
  }

  public class AggregationStage {

    private final AggregationType aggregationType;

    private final Conditional[] conditionalClasses;

    private Conditional.ConditionalMatchType conditionalMatchType;

    public AggregationStage(AggregationType aggregationType, Conditional[] conditionClass, Conditional.ConditionalMatchType conditionalMatchType) {
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

    public boolean allowStage() {
      return processorUtils.allowStage(conditionalClasses, conditionalMatchType, method,
                                       mongoParameterAccessor, convertingParameterAccessor);
    }

    public boolean allowStage(Conditional[] conditionals, Conditional.ConditionalMatchType type) {
      return processorUtils.allowStage(conditionals, type, method,
          mongoParameterAccessor, convertingParameterAccessor);
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

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean isPageable() {
    return mongoParameterAccessor.getPageable() != null;
  }

  @Override
  public Pageable getPageable() {
    return mongoParameterAccessor.getPageable();
  }

  @Override
  public List<String> modifyAggregateQueryPipeline(String query, int stage){
    aggregateQueryPipeline.add(stage, query);
    return aggregateQueryPipeline;
  }
}
