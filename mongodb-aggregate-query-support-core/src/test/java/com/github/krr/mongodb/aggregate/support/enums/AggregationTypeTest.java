package com.github.krr.mongodb.aggregate.support.enums;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Matcher;

import static com.github.krr.mongodb.aggregate.support.enums.AggregationType.QUOTE_TYPE.*;
import static org.testng.Assert.*;

public class AggregationTypeTest {

  @DataProvider
  public Object[][] questionMarkFixtures() {
    return new Object[][] {
      new Object[] {"{\"tag\"  : '?0'}", SINGLE_QUOTE},
      new Object[] {"{\"tag\"  : '?0   '}", SINGLE_QUOTE},
      new Object[] {"{\"tag\"  : '    ?0   '}", SINGLE_QUOTE},
      new Object[] {"{\"tag\"  : ?0  }", NOT_QUOTED},
      new Object[] {"{\"tag\"  :?0}", NOT_QUOTED},
    };
  }

  @DataProvider
  public Object[][] atPhFixtures() {
    return new Object[][] {
      new Object[] {"\"@6\" : { $exists: true, $ne : []}", true, DOUBLE_QUOTE},
      new Object[] {"\"@11\" : { $exists: true, $ne : []}", true, DOUBLE_QUOTE},
      new Object[] {"\"   @11   \" : { $exists: true, $ne : []}", true, DOUBLE_QUOTE},
      new Object[] {"\"assets.@11\" : { $exists: true, $ne : []}", true, DOUBLE_QUOTE},
      new Object[] {"\"   assets.@11   \" : { $exists: true, $ne : []}", true, DOUBLE_QUOTE},
      new Object[] {"'@6' : { $exists: true, $ne : []}", true, SINGLE_QUOTE},
      new Object[] {"'@11' : { $exists: true, $ne : []}", true, SINGLE_QUOTE},
      new Object[] {"'   @11   ' : { $exists: true, $ne : []}", true, SINGLE_QUOTE},
      new Object[] {"'assets.@11' : { $exists: true, $ne : []}", true, SINGLE_QUOTE},
      new Object[] {"'   assets.@11   ' : { $exists: true, $ne : []}", true, SINGLE_QUOTE},
      // no quotes around ph
      new Object[] {"assets.@11 : { $exists: true, $ne : []}", false, NOT_QUOTED},
      new Object[] {"@11 : { $exists: true, $ne : []}", false, NOT_QUOTED},
    };
  }

  @Test(dataProvider = "questionMarkFixtures")
  public void mustParseQuestionMarkPh(String query, AggregationType.QUOTE_TYPE quoteType) {
    Matcher matcher = AggregationType.PLACEHOLDER_REGEX.matcher(query);
    boolean found = matcher.find();
    assertTrue(found);
    AggregationType.QUOTE_TYPE validQuotes = AggregationType.getQuestionMarkPhQuotes(query, matcher);
    Assert.assertEquals(validQuotes, quoteType);
  }

  @Test(dataProvider = "atPhFixtures")
  public void mustParseAtPh(String query, boolean valid, AggregationType.QUOTE_TYPE quoteType) {
    Matcher matcher = AggregationType.PLACEHOLDER_REGEX.matcher(query);
    boolean found = matcher.find();
    assertTrue(found);
    try {
      AggregationType.QUOTE_TYPE actualQuoteType = AggregationType.getAtPhHasQuotes(query, matcher);
      Assert.assertEquals(actualQuoteType, quoteType);
    }
    catch(IllegalArgumentException e) {
      assertFalse(valid);
    }
  }
}