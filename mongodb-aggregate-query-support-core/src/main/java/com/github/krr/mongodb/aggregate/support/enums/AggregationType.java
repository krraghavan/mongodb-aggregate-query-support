package com.github.krr.mongodb.aggregate.support.enums;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.api.BoundParameterValue;
import com.mongodb.internal.VisibleForTesting;
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

public enum AggregationType implements BoundParameterValue {
  MATCH("$match", Match.class) {
    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      // if a match stage contains a placeholder, it can be @@<index of parameter>, or ?<index of parameter>
      boolean containsAtAtPlaceholder = query.matches("@@[0-9]+$");
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
      if (!validPlaceholder) {
        throw new IllegalArgumentException("$limit can only have ? placeholders with the index" +
                                           " of the method parameter that provides the limit value.  Query " + query
                                           + " does not match ?[0-9]+$.");
      }
      int indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "?", query);
      Object value = methodParameterValues[indexOfParameter];
      if (!(value instanceof Integer)) {
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
      if (!onlyContainsAtAtPlaceholder) {
        throw new IllegalArgumentException(
            "$sort with @@ placeholders must only contain the placeholder with the index" +
            " of the method parameter that provides the sort string.  Query " + query
            + " does not match /@@[0-9]+$/.  Did you use embedded quotes ' or \"?");
      }
      int indexOfParameter = AggregationType.getParameterIndex(methodParameterValues, "@@", query);
      Object value = methodParameterValues[indexOfParameter];
      if (!(value instanceof String)) {
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
  OUT("$out", Out.class) {
    private static final String OUT_PH_REGEX = "^(?<startQuote>[\\Q'|\"\\E]?)(?<placeholder>\\Q?\\E)(?<index>[0-9]+)(?<endQuote>[\\Q'|\"\\E]?)$";

    private final Pattern PATTERN = Pattern.compile(OUT_PH_REGEX);

    @Override
    public String getValue(Object[] methodParameterValues, String query,
                           BiFunction<Integer, Class<?>, String> valueProviderFn) {
      boolean containsPlaceholder = containsPlaceholder(query);
      if (!containsPlaceholder) {
        return query;
      }
      Matcher matcher = PATTERN.matcher(query);
      boolean validPlaceholder = matcher.matches();
      if (!validPlaceholder) {
        throw new IllegalArgumentException("$out can only have ? placeholders with the index" +
                                           " of the method parameter that provides the limit value.  Query " + query
                                           + " does not match ?[0-9]+$.");
      }
      int indexOfParameter = Integer.parseInt(matcher.group("index"));
      if (indexOfParameter >= methodParameterValues.length) {
        throw new ArrayIndexOutOfBoundsException("Method has only " + methodParameterValues.length + " parameters " +
                                                 "(0 based)but the placeholder requested parameter at index "
                                                 + indexOfParameter + " at or near " + matcher.end("index"));
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
  MERGE("$merge", Merge.class);

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

  public Class<? extends Annotation> getAnnotationClass() {
    return annotationClass;
  }

  public String getRepresentation() {
    return representation;
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