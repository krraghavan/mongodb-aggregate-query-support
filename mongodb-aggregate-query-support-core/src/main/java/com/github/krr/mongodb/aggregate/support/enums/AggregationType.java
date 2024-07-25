package com.github.krr.mongodb.aggregate.support.enums;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.api.BoundParameterValue;
import com.mongodb.internal.VisibleForTesting;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.krr.mongodb.aggregate.support.enums.AggregationType.QUOTE_TYPE.*;

/**
 * Created by rkolliva
 * 4/25/18.
 */

@Getter
public enum AggregationType implements BoundParameterValue {


  // Use ADD_FIELDS instead
  @Deprecated
  ADDFIELDS("$addFields", AddFields.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query, BiFunction<Integer, Class<?>, String> valueProviderFn) {
      return addFieldsGetValue(methodParameterValues, query, valueProviderFn);
    }
  },
  ADD_FIELDS("$addFields", AddFields.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query, BiFunction<Integer, Class<?>, String> valueProviderFn) {
      return addFieldsGetValue(methodParameterValues, query, valueProviderFn);
    }
  },

  BUCKET("$bucket", Bucket.class),

  // Use BUCKET_AUTO
  @Deprecated
  BUCKETAUTO("$bucketAuto", BucketAuto.class),
  BUCKET_AUTO("$bucketAuto", BucketAuto.class),

  COUNT("$count", Count.class),

  FACET("$facet", Facet.class),

  GROUP("$group", Group.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      boolean containsAtAtPlaceholder = isAtAtPlaceholder(query);
      int indexOfParameter;
      if (containsAtAtPlaceholder) {
        indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "@@", query);
        Object value = methodParameterValues[indexOfParameter];
        if (!(value instanceof String)) {
          throw new IllegalArgumentException("@@ placeholder for $match stages must be bound to string values only. " +
                                             "Placeholder @@ is bound to parameter at index " +
                                             indexOfParameter + " which is of type " + value.getClass());
        }
        return valueProviderFn.apply(indexOfParameter, String.class);
      }
      return IMPLEMENTATION.getValue(methodParameterValues, query, valueProviderFn);
    }
  },

  // Use GRAPH_LOOKUP
  @Deprecated
  GRAPHLOOKUP("$graphLookup", GraphLookup.class),
  GRAPH_LOOKUP("$graphLookup", GraphLookup.class),

  LIMIT("$limit", Limit.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueCbFn) {
      return getIntOrLongValueForQmarkPh(LIMIT, methodParameterValues, query, valueCbFn);
    }
  },

  LOOKUP("$lookup", Lookup.class),

  MATCH("$match", Match.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      // if a match stage contains a placeholder, it can be @@<index of parameter>, or ?<index of parameter>
      boolean containsAtAtPlaceholder = isAtAtPlaceholder(query);
      int indexOfParameter;
      if (containsAtAtPlaceholder) {
        indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "@@", query);
        Object value = methodParameterValues[indexOfParameter];
        if (!(value instanceof String)) {
          throw new IllegalArgumentException("@@ placeholder for $match stages must be bound to string values only. " +
                                             "Placeholder @@ is bound to parameter at index " +
                                             indexOfParameter + " which is of type " + value.getClass());
        }
        // anything else returned as is and may fail during query execution if invalid.
        String matchString = valueProviderFn.apply(indexOfParameter, String.class);
        if (matchString.matches("\\s*\\{\\s*}\\s*$")) {
          // empty {} -> return null
          return null;
        }
        // an invalid match string will fail at query time.
        return matchString;
      }
      return IMPLEMENTATION.getValue(methodParameterValues, query, valueProviderFn);
    }
  },

  OUT("$out", Out.class) {

    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      Matcher matcher = PATTERN.matcher(query);
      if(!matcher.matches()) {
        throw new IllegalArgumentException("$out can only have ? placeholders with the index" +
                                           " of the method parameter that provides the collection name.  Query " +
                                           query + " does not match " + OUT_PH_REGEX);
      }
      String indexStr = matcher.group("index");
      int indexOfParameter;
      try {
        indexOfParameter = Integer.parseInt(indexStr);
      }
      catch (NumberFormatException e) {
        throw new IllegalArgumentException("Index of placeholder " + query + " is not an integer", e);
      }
      Object value = methodParameterValues[indexOfParameter];
      if (!(value instanceof String)) {
        throw new IllegalArgumentException("Placeholder for $out stages must be bound to string values only. " +
                                           "Placeholder + ? " + " is bound to parameter at index " +
                                           indexOfParameter + " which is of type " + value.getClass());
      }
      String outCollectionName = valueProviderFn.apply(indexOfParameter, String.class);
      return String.format("\"%s\"", outCollectionName);
    }
  },

  PROJECT("$project", Project.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      // project can have either ? or @@ placeholders - find out which one this is
      boolean containsAtAtPlaceholder = isAtAtPlaceholder(query);
      if(containsAtAtPlaceholder) {
        return AggregationType.getStringAtAtPlaceholders(PROJECT, methodParameterValues, query, valueProviderFn);
      }
      else {
        return IMPLEMENTATION.getValue(methodParameterValues, query, valueProviderFn);
      }
    }
  },
  // use REPLACE_ROOT instead
  @Deprecated
  REPLACEROOT("$replaceRoot", ReplaceRoot.class),
  REPLACE_ROOT("$replaceRoot", ReplaceRoot.class),

  SORT("$sort", Sort.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      // project can have either ? or @@ placeholders - find out which one this is
      boolean containsAtAtPlaceholder = isAtAtPlaceholder(query);
      if(containsAtAtPlaceholder) {
        return AggregationType.getStringAtAtPlaceholders(PROJECT, methodParameterValues, query, valueProviderFn);
      }
      else {
        return IMPLEMENTATION.getValue(methodParameterValues, query, valueProviderFn);
      }
    }

    @Override
    public boolean stringNeedsQuoting() {
      return false;
    }
  },

  SKIP("$skip", Skip.class) {

    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      return getIntOrLongValueForQmarkPh(SKIP, methodParameterValues, query, valueProviderFn);
    }
  },

  // Use SORT_BY_COUNT instead
  @Deprecated
  SORTBYCOUNT("$sortByCount", SortByCount.class),
  SORT_BY_COUNT("$sortByCount", SortByCount.class),

  UNWIND("$unwind", Unwind.class),

  MERGE("$merge", Merge.class)

  ;

  String addFieldsGetValue(Object[] methodParameterValues, String query, BiFunction<Integer, Class<?>, String> valueProviderFn) {
    boolean containsPlaceholder = containsPlaceholder(query);
    if (!containsPlaceholder) {
      return query;
    }
    // if a match stage contains a placeholder, it can be @@<index of parameter>, or ?<index of parameter>
    boolean containsAtAtPlaceholder = isAtAtPlaceholder(query);
    int indexOfParameter;
    if (containsAtAtPlaceholder) {
      indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "@@", query);
      Object value = methodParameterValues[indexOfParameter];
      if (!(value instanceof String)) {
        throw new IllegalArgumentException("@@ placeholder for $addFields stages must be bound to string values only. " +
                                           "Placeholder @@ is bound to parameter at index " +
                                           indexOfParameter + " which is of type " + value.getClass());
      }
      // anything else returned as is and may fail during query execution if invalid.
      // an invalid match string will fail at query time.
      return valueProviderFn.apply(indexOfParameter, String.class);
    }
    return IMPLEMENTATION.getValue(methodParameterValues, query, valueProviderFn);
  }

  private static boolean isAtAtPlaceholder(String query) {
    return query.matches("@@[0-9]+$");
  }

  private static boolean isQmarkPh(String query) {
    return query.matches("[\\Q'|\"\\E]?\\?[0-9]+[\\Q'|\"\\E]?$");
  }

  private static final String OUT_PH_REGEX = "^(?<startQuote>[\\Q'|\"\\E]?)(?<placeholder>\\Q?\\E)(?<index>[0-9]+)(?<endQuote>[\\Q'|\"\\E]?)$";

  private static final Pattern PATTERN = Pattern.compile(OUT_PH_REGEX);

  /**
   * Processes an @@ placeholder type for aggregation stages that just provide the replacement as an entire
   * String
   * @param methodParameterValues - the argument list to the methods
   * @param query - the query string
   * @param valueProviderFn - the valueProviderFn that gives the actual value of the parameter
   * @return - the string that replaces the placeholder.
   */
  private static String getStringAtAtPlaceholders(AggregationType aggType, Object[] methodParameterValues, String query,
                                                      BiFunction<Integer, Class<?>, String> valueProviderFn) {
    String type = aggType.getRepresentation();

    // if the stage contains a placeholder, it must only contain @@<index of parameter>
    boolean onlyContainsAtAtPlaceholder = isAtAtPlaceholder(query);
    if (!onlyContainsAtAtPlaceholder) {
      throw new IllegalArgumentException(type + " with @@ placeholders must only contain the placeholder with the index" +
          " of the method parameter that provides the " + type + " string.  Query " + query
          + " does not match /@@[0-9]+$/.  Did you use embedded quotes ' or \"?");
    }
    int indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "@@", query);
    Object value = methodParameterValues[indexOfParameter];
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Placeholder for " + type + " stages must be bound to string values only. " +
                                         "Placeholder + @@ " + " is bound to parameter at index " +
                                         indexOfParameter + " which is of type " + value.getClass());
    }

    // anything else returned as is and may fail during query execution if invalid.
    return valueProviderFn.apply(indexOfParameter, String.class);
  }

  @VisibleForTesting(otherwise = VisibleForTesting.AccessModifier.PRIVATE)
  static String getIntOrLongValueForQmarkPh(AggregationType aggType, Object[] methodParameterValues, String query,
                                            BiFunction<Integer, Class<?>, String> valueCbFn) {
    String type = aggType.getRepresentation();
    boolean containsPlaceholder = containsPlaceholder(query);
    if (!containsPlaceholder) {
      return query;
    }
    int indexOfParameter = getQmarkPhIndex(aggType, methodParameterValues, query);
    Object value = methodParameterValues[indexOfParameter];
    // aggregation types calling this method only take int or long values.  The returned value is always a string
    // representation of the long/int value
    if (!(value instanceof Integer || value instanceof Long)) {
      throw new IllegalArgumentException("Placeholder for " + type + " stages must be bound to integer/long values only. " +
                                         "Placeholder + ? " + " is bound to parameter at index " +
                                         indexOfParameter + " which is of type " + value.getClass());
    }
    return valueCbFn.apply(indexOfParameter, value.getClass());
  }

  private static int getQmarkPhIndex(AggregationType type, Object[] methodParameterValues, String query) {
    boolean isQmarkPh = isQmarkPh(query);
    if (!isQmarkPh) {
      throw new IllegalArgumentException(type.representation + " can only have ? placeholders with the index" +
                                         " of the method parameter that provides the " + type + " value.  Query " +
                                         query + " does not match ?[0-9]+$.");
    }
    // if the ph has '' or "" get it without them
    return AggregationType.getParameterIndex(methodParameterValues, "?", query);
  }

  /**
   * @param queryWithPlaceholders - the query string with placeholders
   * @param matcher               - the regex matcher that was used to detect the placeholder
   * @return - true if the string is properly quoted - false otherwise.
   */
  @VisibleForTesting(otherwise = VisibleForTesting.AccessModifier.PRIVATE)
  static QUOTE_TYPE validateQuotes(String queryWithPlaceholders, Matcher matcher, String placeholder) {
    // the location of the index of the placeholder
    if ("?".equals(placeholder)) {
      // ? placeholders can be quoted or not.
      return getQuestionMarkPhQuotes(queryWithPlaceholders, matcher);
    }
    else if ("@".equals(placeholder)) {
      // @ placeholders must be quoted.
      return getAtPhHasQuotes(queryWithPlaceholders, matcher);
    }
    else if ("@@".equals(placeholder)) {
      // @ placeholders must be quoted.
      return getAtAtPhQuotes(queryWithPlaceholders, matcher);
    }
    throw new UnsupportedOperationException("Unsupported placeholder " + placeholder + " for validating quotes in " +
                                            " query: " + queryWithPlaceholders + ".  Valid placeholders are " +
                                            "?, @ (only on keys) and @@ (replaces whole string)");
  }

  private static QUOTE_TYPE getAtAtPhQuotes(String query, Matcher matcher) {
    int endIndex = matcher.end("index");
    QUOTE_TYPE endQuoteType = endQuoteType(query, endIndex);
    QUOTE_TYPE startQuoteType = startQuoteType(query, endIndex);
    if (startQuoteType == NOT_QUOTED) {
      throw new IllegalArgumentException("@@ placeholders must be quoted with ' or \" at or near " + endIndex);
    }
    else if (startQuoteType != endQuoteType) {
      throw new IllegalArgumentException("@@ placeholders has mismatched ' or \" at or near " + endIndex);
    }
    return startQuoteType;
  }

  /**
   * @ placeholder are placeholders on the left (key).  They can be of the following forms
   * {@code @nn} - simple form - the parameter at index nn is the key name
   * abc.@nn - placeholder using parameter at index nn who's value is an attribute of abc
   * \"abc.@nn\" | \'abc.@nn\' - Quoted version of abc.@nn
   */
  static QUOTE_TYPE getAtPhHasQuotes(String query, Matcher matcher) {
    int endIndex = matcher.end("index");
    QUOTE_TYPE endQuoteType = endQuoteType(query, endIndex);
    QUOTE_TYPE startQuoteType = startQuoteType(query, endIndex);
    if (startQuoteType == NOT_QUOTED) {
      throw new IllegalArgumentException("@ placeholders must be quoted with ' or \" at or near " + endIndex);
    }
    else if (startQuoteType != endQuoteType) {
      throw new IllegalArgumentException("@ placeholders has mismatched ' or \" at or near " + endIndex);
    }
    return startQuoteType;
  }

  /**
   * ? placeholders always have the form ?nn where nn is the index.  If
   * the character after the placeholder (not counting whitespace) is a
   * ' or ' then validate that the non whitespace char before the placeholder
   * is also a ' or "
   */
  static QUOTE_TYPE getQuestionMarkPhQuotes(String queryWithPlaceholders, Matcher matcher) {
    int endIndex = matcher.end("index");
    QUOTE_TYPE endQuoteType = endQuoteType(queryWithPlaceholders, endIndex - 1);
    QUOTE_TYPE startQuoteType = startQuoteType(queryWithPlaceholders, endIndex - 1);
    // must have matching quotes or not at all.
    if (startQuoteType == endQuoteType) {
      return startQuoteType;
    }
    throw new IllegalArgumentException("Mismatched quotes at or near " + endIndex + ".  Start quote is of type "
    + startQuoteType + " while endQuote is of type " + endQuoteType);
  }

  private static QUOTE_TYPE startQuoteType(String query, int endIndex) {
    QUOTE_TYPE quoted = NOT_QUOTED;
    int i = 1;
    boolean done = endIndex == 0;
    while (!done) {
      char prevChar = query.charAt(endIndex - i);
      if (prevChar == '\'') {
        // this is a quote
        done = true;
        quoted = SINGLE_QUOTE;
      }
      else if(prevChar == '\"') {
        done = true;
        quoted = DOUBLE_QUOTE;
      }
      else if (prevChar == '{' || prevChar == '[' || prevChar == ':' || prevChar == ',') {
        done = true;
      }
      else if ((endIndex - i) == 0) {
        done = true;
      }
      i++;
    }
    return quoted;
  }

  /**
   * A section is terminated when we encounter ':', '}', ']' or end of string
   * if we don't see a ' or " then the section is not quoted.
   */
  static QUOTE_TYPE endQuoteType(String query, int endIndex) {
    QUOTE_TYPE quoted = NOT_QUOTED;
    int stringLength = query.length();
    int i = 0;
    boolean done = endIndex == stringLength;
    while (!done) {
      char nextChar = query.charAt(endIndex + i);
      if (nextChar == '\'') {
        // this is a quote
        done = true;
        quoted = QUOTE_TYPE.SINGLE_QUOTE;
      }
      else if(nextChar == '\"') {
        quoted = QUOTE_TYPE.DOUBLE_QUOTE;
        done = true;
      }
      else if (nextChar == ':' || nextChar == '}' || nextChar == ']' || nextChar == ',') {
        done = true;
      }
      else if (endIndex + i == stringLength) {
        done = true;
      }
      i++;
    }
    return quoted;
  }

  @VisibleForTesting(otherwise = VisibleForTesting.AccessModifier.PRIVATE)
  static final BoundValueImpl IMPLEMENTATION = new BoundValueImpl();

  @Override
  public String getValue(Object[] methodParameterValues, String query,
                         BiFunction<Integer, Class<?>, String> valueProviderFn) {
    return IMPLEMENTATION.getValue(methodParameterValues, query, valueProviderFn);
  }

  private static boolean containsPlaceholder(String query) {
    //noinspection RegExpDuplicateCharacterInClass,RegExpRedundantEscape
    return query.matches("(.*)[@@|\\?|@]+(.*)");
  }

  private static int getParameterIndex(Object[] methodParameterValues, String placeholder, String query) {
    String indexValue = query.substring(placeholder.length());
    try {
      int index = Integer.parseInt(indexValue);
      int maxParams = methodParameterValues.length;
      if (index >= maxParams) {
        throw new IllegalArgumentException("Method has only " + maxParams + " parameters but the placeholder " +
                                           " index for query " + query + " was " + index);
      }
      return index;
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("Index of placeholder " + placeholder + " for query " + query
                                         + " is not an integer", e);
    }
  }

  private final String representation;

  private final Class<? extends Annotation> annotationClass;

  enum QUOTE_TYPE {
    SINGLE_QUOTE,
    DOUBLE_QUOTE,
    NOT_QUOTED
  }

  @SuppressWarnings("RegExpDuplicateCharacterInClass")
  private static final String PLACEHOLDER_MATCHER =
      //^(?<startQuote>[\Q'|"\E]?)(?<placeholder>\Q?\E)(?<index>[0-9]+)(?<endQuote>[\Q'|"\E]?)$
      "(?<everythingBeforePh>[^@|@@|?]*)(?<placeholder>[@|@@|?]+)(?<index>[0-9]+)(?<restOfString>.*$)";

  @VisibleForTesting(otherwise = VisibleForTesting.AccessModifier.PRIVATE)
  static final Pattern PLACEHOLDER_REGEX = Pattern.compile(PLACEHOLDER_MATCHER,
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

  static class BoundValueImpl implements BoundParameterValue {
    @SuppressWarnings({"unchecked", "ArraysAsListWithZeroOrOneArgument", "rawtypes"})
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      // keep a copy
      @SuppressWarnings("StringOperationCanBeSimplified")
      String queryWithPlaceholders = new String(query);
      Matcher matcher = PLACEHOLDER_REGEX.matcher(query);
      boolean done = false;
      while (matcher.find() && !done) {
        String indexStr = matcher.group("index");
        if (indexStr == null) {
          done = true;
        }
        else {
          int index = Integer.parseInt(indexStr);
          String placeholder = matcher.group("placeholder");
          if (placeholder == null) {
            continue;
          }
          else {
            QUOTE_TYPE quoted = validateQuotes(queryWithPlaceholders, matcher, placeholder);
            int numParameters = methodParameterValues.length;
            if (index >= numParameters) {
              throw new IllegalArgumentException("Parameter index " + index + " is invalid for query " + query +
                                                 ".  Method has only " + numParameters + " parameters");
            }
            if (isArrayOrCollection(methodParameterValues, index) && quoted != NOT_QUOTED) {
              // if we get a collection type which is quoted, just make sure that the elements
              // of the array are quoted.
              Object value = methodParameterValues[index];
              Collection<String> values;
              if (value.getClass().isArray()) {
                values = (List) Arrays.asList(value);
              }
              else {
                values = (Collection) value;
              }
              values = values.stream().map(v -> {
                if (v.charAt(0) != '"') {
                  return "\"".concat(v).concat("\"");
                }
                return v;
              }).collect(Collectors.toList());
              String placeholderValue = StringUtils.join(values, ",");
              query = query.replace(String.format("'%s%d'", placeholder, index),
                                    "[".concat(placeholderValue).concat("]"));
            }
            else {
              String value = valueProviderFn.apply(index, getValueClass(methodParameterValues, index));
              String quote = "";
              if (isString(methodParameterValues, index) && quoted == NOT_QUOTED && stringNeedsQuoting()) {
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