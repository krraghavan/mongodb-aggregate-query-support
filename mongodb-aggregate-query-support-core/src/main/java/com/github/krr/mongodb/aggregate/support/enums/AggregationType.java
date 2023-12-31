package com.github.krr.mongodb.aggregate.support.enums;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.api.BoundParameterValue;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by rkolliva
 * 4/25/18.
 */

public enum AggregationType implements BoundParameterValue {
  MATCH("$match", Match.class),
  GROUP("$group", Group.class),
  UNWIND("$unwind", Unwind.class),
  LOOKUP("$lookup", Lookup.class),
  PROJECT("$project", Project.class),
  LIMIT("$limit", Limit.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueCbFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      boolean validPlaceholder = query.matches("\\?[0-9]+$");
      if(!validPlaceholder) {
        throw new IllegalArgumentException("$limit can only have ? placeholders with the index" +
                                           " of the method parameter that provides the limit value.  Query " + query
                                           + " does not match ?[0-9]+$.");
      }
      int indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "?", query);
      Object value = methodParameterValues[indexOfParameter];
      if(!(value instanceof Integer)) {
        throw new IllegalArgumentException("Placeholder for $limit stages must be bound to integer values only. " +
                                           "Placeholder + ? " + " is bound to parameter at index " +
                                           indexOfParameter + " which is of type " + value.getClass());
      }
      return valueCbFn.apply(indexOfParameter, Integer.class);
    }
  },
  BUCKET("$bucket", Bucket.class),
  ADDFIELDS("$addFields", AddFields.class),
  REPLACEROOT("$replaceRoot", ReplaceRoot.class),
  SORT("$sort", Sort.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      // if a sort stage contains a placeholder, it must only contain @@<index of parameter>
      boolean onlyContainsAtAtPlaceholder = query.matches("@@[0-9]+$");
      if(!onlyContainsAtAtPlaceholder) {
        throw new IllegalArgumentException("$sort with @@ placeholders must only contain the placeholder with the index" +
                                           " of the method parameter that provides the sort string.  Query " + query
                                           + " does not match @@[0-9]+$");
      }
      int indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "@@", query);
      Object value = methodParameterValues[indexOfParameter];
      if(!(value instanceof String)) {
        throw new IllegalArgumentException("Placeholder for $sort stages must be bound to string values only. " +
                                           "Placeholder + @@ " + " is bound to parameter at index " +
                                           indexOfParameter + " which is of type " + value.getClass());
      }

      // anything else returned as is and may fail during query execution if invalid.
      return valueProviderFn.apply(indexOfParameter, String.class);
    }

    @Override
    public boolean stringNeedsQuoting() {
      return false;
    }
  },
  SORTBYCOUNT("$sortByCount", SortByCount.class),
  BUCKETAUTO("$bucketAuto", BucketAuto.class),
  GRAPHLOOKUP("$graphLookup", GraphLookup.class),
  FACET("$facet", Facet.class),
  COUNT("$count", Count.class),
  SKIP("$skip", Skip.class),
  OUT("$out", Out.class),
  MERGE("$merge", Merge.class)

  ;

  private static final BoundValueImpl IMPLEMENTATION = new BoundValueImpl();

  @Override
  public String getValue(Object[] methodParameterValues, String query,
                         BiFunction<Integer, Class<?>, String> valueProviderFn) {
    return IMPLEMENTATION.getValue(methodParameterValues, query, valueProviderFn);
  }

  private static boolean containsPlaceholder(String query) {
    return query.matches("(.*)[@@|\\?|@]+(.*)");
  }

  private static int getParameterIndex(Object[] methodParameterValues, String placeholder, String query) {
    String indexValue = query.substring(placeholder.length());
    try {
      int index = Integer.parseInt(indexValue);
      int maxParams = methodParameterValues.length;
      if(index >= maxParams) {
        throw new IllegalArgumentException("Method has only " + maxParams + " parameters but the placeholder " +
                                           " index for query " + query + " was " + index);
      }
      return index;
    }
    catch(NumberFormatException e) {
      throw new IllegalArgumentException("Index of placeholder " + placeholder + " for query " + query
                                         + " is not an integer", e);
    }
  }

  private final String representation;

  private final Class<? extends Annotation> annotationClass;

  private static final String PLACEHOLDER_MATCHER =
      "(?<everythingBeforePh>[^@|?]*)(?<placeholder>[@|@@|?]+)?(?<index>[0-9]+)(?<restOfString>.*)$";
  private static final Pattern PLACEHOLDER_REGEX = Pattern.compile(PLACEHOLDER_MATCHER,
                                                                   Pattern.CASE_INSENSITIVE |
                                                                   Pattern.MULTILINE |
                                                                   Pattern.DOTALL);

  AggregationType(String representation, Class<? extends Annotation> annotationClass) {
    this.representation = representation;
    this.annotationClass = annotationClass;
  }

  public static AggregationType from(Annotation annotation) {
    return from(annotation.annotationType());
  }

  public static AggregationType from(Class<? extends Annotation> stageType) {
    for (AggregationType type : values()) {
      if (type.getAnnotationClass() == stageType) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown annotation type " + stageType.getName());
  }

  public Class<? extends Annotation> getAnnotationClass() {
    return annotationClass;
  }

  public String getRepresentation() {
    return representation;
  }

  static class BoundValueImpl implements BoundParameterValue {
    @SuppressWarnings({"unchecked", "ArraysAsListWithZeroOrOneArgument", "rawtypes", "DataFlowIssue"})
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      // keep a copy
      String queryWithPlaceholders = new String(query);
      Matcher matcher = PLACEHOLDER_REGEX.matcher(query);
      boolean done = false;
      while (matcher.find() && !done) {
        String indexStr = matcher.group("index");
        if(indexStr == null) {
          done = true;
        }
        else {
          int index = Integer.parseInt(indexStr);
          String placeholder = matcher.group("placeholder");
          if (placeholder == null) {
            continue;
          }
          else {
            char charBeforePlaceholder = queryWithPlaceholders.charAt(matcher.end("placeholder") - 2);
            boolean startSingleQuote = charBeforePlaceholder == '\'';
            boolean startDoubleQuote = charBeforePlaceholder == '\"';
            int endIndex = matcher.end("index");
            if ((startSingleQuote || startDoubleQuote) && (endIndex == query.length())) {
              // reached end of string without a closing quote
              throw new IllegalArgumentException("Unterminated quoted string at or near index " + endIndex);
            }
            boolean quoted = false;
            if (endIndex < query.length()) {
              char charAfterIndex = queryWithPlaceholders.charAt(endIndex);
              boolean endSingleQuote = charAfterIndex == '\'';
              boolean endDoubleQuote = charAfterIndex == '\"';
              if ((startSingleQuote && !endSingleQuote) || (!startDoubleQuote && endDoubleQuote) ||
                  (!startSingleQuote && endSingleQuote) || (startDoubleQuote && !endDoubleQuote)) {
                throw new IllegalArgumentException("Query " + query + " has mismatched quotes at or near " + endIndex);
              }
              else if (startSingleQuote || startDoubleQuote) {
                quoted = true;
              }
            }
            int numParameters = methodParameterValues.length;
            if (index >= numParameters) {
              throw new IllegalArgumentException("Parameter index " + index + " is invalid for query " + query +
                                                 ".  Method has only " + numParameters + " parameters");
            }
            if(isArrayOrCollection(methodParameterValues, index) && quoted) {
              // if we get a collection type which is quoted, just make sure that the elements
              // of the array are quoted.
              Object value = methodParameterValues[index];
              Collection<String> values;
              if(value.getClass().isArray()) {
                values = (List)Arrays.asList(value);
              }
              else {
                values = (Collection)value;
              }
              values = values.stream().map(v ->  {
                if(v.charAt(0) != '"') {
                  return "\"".concat(v).concat("\"");
                }
                return v;
              }).collect(Collectors.toList());
              String placeholderValue = StringUtils.join(values, ",");
              query = query.replace(String.format("'%s%d'", placeholder, index), "[".concat(placeholderValue).concat("]"));
            }
            else {
              String value = valueProviderFn.apply(index, getValueClass(methodParameterValues, index));
              String quote = "";
              if (isString(methodParameterValues, index) && !quoted && stringNeedsQuoting()) {
                quote = "\"";
              }
              query = query.replace(String.format("%s%d", placeholder, index), quote.concat(value).concat(quote));
            }
          }
        }
        queryWithPlaceholders = matcher.group("restOfString");
        matcher = PLACEHOLDER_REGEX.matcher(queryWithPlaceholders);
      }
      return query;
    }

  }

}