package com.github.krr.mongodb.aggregate.support.query;

import com.github.krr.mongodb.aggregate.support.query.AbstractAggregateQueryProvider.ParameterBinding;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rkolliva
 * 4/25/18.
 * <p>
 * A parser that extracts the parameter bindings from a given query string.
 */
public abstract class ParameterBindingParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterBindingParser.class);

  private static final String PARAMETER_PREFIX = "_param_";
  private static final String QUOTED_PARSEABLE_PARAMETER = "\"" + PARAMETER_PREFIX + "$2\"";
  private static final String PARSEABLE_PARAMETER = "\"" + PARAMETER_PREFIX + "$1\"";
  private static final Pattern PARAMETER_BINDING_PATTERN = Pattern.compile("(?<beginQuote>[\"|'])?(?<prefixToPrefix>[\\w\\.]*)?(?<prefix>[\\?|@])(?<index>\\d+)(?<endQuote>[\"|'])?");
  private static final Pattern PARSEABLE_BINDING_PATTERN = Pattern.compile("(?<beginQuote>[\"|'])?" + PARAMETER_PREFIX + "(\\d+)(?<endQuote>[\"|'])?");

  private static final String LHS_PARAMETER_PREFIX = "@lhs@";
  private static final String LHS_PARSEABLE_PARAMETER = LHS_PARAMETER_PREFIX + "$1";
  private static final Pattern LHS_PARAMETER_BINDING_PATTERN = Pattern.compile("@(\\d+)");
  private static final Pattern LHS_PARSEABLE_BINDING_PATTERN = Pattern.compile(LHS_PARAMETER_PREFIX + "(\\d+)?");

  private static final int PARAMETER_INDEX_GROUP = 2;

  public ParameterBindingParser() {
  }

  /**
   * Returns a list of {@link ParameterBinding}s found in the given {@code input} or an
   * {@link Collections#emptyList()}.
   *
   * @param input - the string with parameter bindings
   * @return - the list of parameters
   */
  public List<ParameterBinding> parseParameterBindingsFrom(String input) {

    if (StringUtils.isEmpty(input)) {
      return Collections.emptyList();
    }

    List<ParameterBinding> bindings = new ArrayList<>();

    Matcher matcher = PARAMETER_BINDING_PATTERN.matcher(input);
    String parseableInput = makeParameterReferencesParseable(matcher, bindings, input);
    Matcher lhsMatcher = LHS_PARAMETER_BINDING_PATTERN.matcher(parseableInput);
    makeParameterReferencesParseable(lhsMatcher, bindings, parseableInput);
    return bindings;
  }

  private String makeParameterReferencesParseable(Matcher matcher, List<ParameterBinding> bindings, String input) {
    String parseableParameter = PARSEABLE_PARAMETER;
    String retval = input;
    while(matcher.find()) {
      boolean beginQuote = matcher.group("beginQuote") != null;
      boolean endQuote = matcher.group("endQuote") != null;
      if(beginQuote != endQuote) {
        LOGGER.error("Invalid syntax for {}}. Mismatched quotes", input);
        throw new IllegalArgumentException("Invalid syntax for " + input + ". Mismatched quotes");
      }
      if(beginQuote) {
        parseableParameter = QUOTED_PARSEABLE_PARAMETER;
      }
      String prefix = matcher.group("prefix");
      ParameterBinding binding = new ParameterBinding(Integer.parseInt(matcher.group("index")), beginQuote, prefix);
      bindings.add(binding);
    }
    retval = matcher.replaceAll(parseableParameter);
    return retval;
  }

  protected void collectParameterReferencesIntoBindings(List<ParameterBinding> bindings,
                                                        Object value) {

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
    else if(value instanceof Binary) {
      // binary types - we need to get the param value and substitute the params
      // The json decoder assumes the value of the string is a Base64 encoded version
      // of the byte [].  Since we have not yet substituted the values we want to check
      // if the parameters needs replacing.  _param_0 gets decoded as a 7 byte [] with
      // values - 0, 10, 90, -83, -87, -128, -48.  and for _param_1 only the last byte
      // changes to -44.  So we first check the byte sequence for the first six bytes
      // and use byte 7 to figure out which parameter value to substitute with if needed
      if(binaryValueNeedsReplacing((Binary) value)) {
        int index = determineBinaryParameterIndex((Binary) value);
        bindings.add(new ParameterBinding(index, true));
      }
    }
    else {
      bindDriverSpecificTypes(bindings, value);
    }
  }

  private int determineBinaryParameterIndex(Binary value) {
    byte [] bytes = value.getData();
    byte lastByte = bytes[6];
    return (lastByte + 48)/4;
  }

  private boolean binaryValueNeedsReplacing(Binary value) {
    final byte [] paramBytes = {0, 10, 90, -83, -87, -128};
    byte [] bytes = value.getData();
    if(bytes.length != paramBytes.length + 1) {
      return false;
    }
    for (int i = 0; i < paramBytes.length; i++) {
      if(bytes[i] != paramBytes[i]) {
        return false;
      }
    }
    return true;
  }

  protected abstract void bindDriverSpecificTypes(List<ParameterBinding> bindings,
                                             Object value);

  protected void potentiallyAddBinding(String source, List<ParameterBinding> bindings) {

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