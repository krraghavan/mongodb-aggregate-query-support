package com.github.krr.mongodb.aggregate.support.enums;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.BiFunction;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class BoundValueImplTest {

  @DataProvider
  public static Object[][] invalidNumericOnlyPhFixtures() {
    Object[] args = {randomAlphabetic(5), RandomStringUtils.randomAlphabetic(10)};
    return new Object[][]{
      new Object[]{AggregationType.LIMIT, args},
      new Object[]{AggregationType.SKIP, args},
    };
  }

  @DataProvider
  private Object[][] queryFixtures() {
    String randomValues1 = randomAlphabetic(10);
    String randomValues2 = randomAlphabetic(10);
    String randomValues3 = "{$sort:{\"".concat(randomAlphabetic(10)).concat("\":-1}}");
    return new Object[][] {
      // ? placeholder
      new Object[]{"{" +
                   "   \"tag\": '?0'" +
                   "}", new Object[] {randomValues1}, "{" +
                        "   \"tag\": '" + randomValues1 + "'" +
                        "}", false},
      // @ placeholder
      new Object[]{"{" +
                   "   \"tag.@0\": '?0'" +
                   "}", new Object[] {randomValues2}, "{" +
                        "   \"tag." + randomValues2 + "\": '" + randomValues2 + "'" +
                        "}", true},
      // only @@ placeholder
      new Object[]{"'@@2'", new Object[] {randomValues1, randomValues2, randomValues3}, "'".concat(randomValues3)
          .concat("'"), false},
      // multiple placeholders
      new Object[]{"{" +
                   "   \"tag\": '?0', \"abc\":'?1'" +
                   "}", new Object[] {randomValues1, randomValues2, randomValues3}, "{" +
                                                                                    "   \"tag\": '" + randomValues1 + "'," +
                                                                                    " \"abc\":'" + randomValues2 + "'" +
                                                                                    "}", false},
      // multiple placeholders - mixed @ and ?
      new Object[]{"{" +
                   "   \"tag\": '?0', \"abc\":'@1'" +
                   "}", new Object[] {randomValues1, randomValues2, randomValues3}, "{" +
                                                                                    "   \"tag\": '" + randomValues1 + "'," +
                                                                                    " \"abc\":'" + randomValues2 + "'" +

                                                                                    "}", false},
      // placeholder on left
      new Object[]{"{" +
                   "   \"@0\": '?1'" +
                   "}", new Object[] {randomValues1, randomValues2, randomValues3}, "{" +
                                                                                    "   \"" + randomValues1 + "\": '" + randomValues2 + "'" +
                                                                                    "}", false},
    };
  }
  @DataProvider
  private Object[][] invalidQueryFixtures() {
    String randomValues1 = randomAlphabetic(10);
    String randomValues2 = randomAlphabetic(10);
    String randomValues3 = "{$sort:{\"".concat(randomAlphabetic(10)).concat("\":-1}}");
    return new Object[][] {
      // ? placeholder
      new Object[]{"{" +
                   "   \"tag\": '?10'" +
                   "}", new Object[] {randomValues1}, "{" +
                        "   \"tag\": '" + randomValues1 + "'" +
                        "}"},
      // @ placeholder
      new Object[]{"{" +
                   "   \"tag\": '@10'" +
                   "}", new Object[] {randomValues2}, "{" +
                        "   \"tag\": '" + randomValues2 + "'" +
                        "}"},
      // only @@ placeholder
      new Object[]{"@@12", new Object[] {randomValues1, randomValues2, randomValues3}, randomValues3},
      // multiple placeholders
      new Object[]{"{" +
                   "   \"tag\": '?10', \"abc\":'?11'" +
                   "}", new Object[] {randomValues1, randomValues2, randomValues3}, "{" +
                                                                                    "   \"tag\": '" + randomValues1 + "'," +
                                                                                    " \"abc\":'" + randomValues2 + "'" +
                                                                                    "}"},
      // multiple placeholders - mixed @ and ?
      new Object[]{"{" +
                   "   \"tag\": '?1', \"abc\":'@11'" +
                   "}", new Object[] {randomValues1, randomValues2, randomValues3}, "{" +
                                                                                    "   \"tag\": '" + randomValues1 + "'," +
                                                                                    " \"abc\":'" + randomValues2 + "'" +

                                                                                    "}"},
      // placeholder on left
      new Object[]{"{" +
                   "   \"@0\": '?11'" +
                   "}", new Object[] {randomValues1, randomValues2, randomValues3}, "{" +
                                                                                    "   \"" + randomValues1 + "\": '" + randomValues2 + "'" +
                                                                                    "}"},
    };
  }

  @DataProvider
  private Object[][] invalidSortQueryFixtures() {
    String randomValues1 = randomAlphabetic(10);
    String randomValues2 = randomAlphabetic(10);
    String randomValues3 = "{$sort:{\"".concat(randomAlphabetic(10)).concat("\":-1}}");
    return new Object[][] {
      // ? placeholder for sort query
      new Object[]{"{" +
                   "   \"tag\": '?10'" +
                   "}", new Object[] {randomValues1}},
      // only @@ placeholder
      new Object[]{"@@12", new Object[] {randomValues1, randomValues2, randomValues3}},
    };
  }
  @DataProvider
  private Object[][] invalidMatchQueryFixtures() {
    String randomValues1 = randomAlphabetic(10);
    String randomValues2 = randomAlphabetic(10);
    String randomValues3 = "{$match:{\"".concat(randomAlphabetic(10)).concat("\":\"")
                                        .concat(randomAlphabetic(15)).concat("\"}}");
    return new Object[][] {
      // ? placeholder for sort query
      new Object[]{"{" +
                   "   \"tag\": '?10'" +
                   "}", new Object[] {randomValues1}},
      // only @@ placeholder
      new Object[]{"@@12", new Object[] {randomValues1, randomValues2, randomValues3}},
    };
  }

  @Test(dataProvider = "queryFixtures")
  public void mustExtractPlaceholderValues(String placeholderQuery, Object[] args, String expectedQuery,
                                           boolean mustQuoteString) {
    AggregationType.BoundValueImpl impl = new AggregationType.BoundValueImpl() {
      @Override
      public boolean stringNeedsQuoting() {
        return mustQuoteString;
      }
    };
    String query = impl.getValue(args, placeholderQuery, (index, valueClass) -> (String) args[index]);
    assertEquals(query, expectedQuery);
  }

  @Test(dataProvider = "invalidQueryFixtures", expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionsForInvalidPlaceholderIndex(String placeholderQuery, Object[] args, String expectedQuery) {
    AggregationType.BoundValueImpl impl = new AggregationType.BoundValueImpl();
    String query = impl.getValue(args, placeholderQuery, (index, valueClass) -> (String) args[index]);
    assertEquals(query, expectedQuery);
  }

  @Test
  public void mustProcessValidAtAtParameterForSort() {
    String expectedQuery = "$sort: '" + randomAlphabetic(5) + "':-1}";
    Object[] args = {randomAlphabetic(5), expectedQuery};
    String query = AggregationType.SORT.getValue(args, "@@1", (index, valueClass) -> (String) args[index]);
    assertEquals(query, expectedQuery);
  }

  @Test(dataProvider = "invalidSortQueryFixtures", expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionForInvalidSort(String query, Object[] args) {
    String actualQuery = AggregationType.SORT.getValue(args, query, (index, valueClass) -> (String) args[index]);
    assertEquals(query, actualQuery);
  }

  @Test(dataProvider = "invalidMatchQueryFixtures", expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionForInvalidMatch(String query, Object[] args) {
    String actualQuery = AggregationType.MATCH.getValue(args, query, (index, valueClass) -> (String) args[index]);
    assertEquals(query, actualQuery);
  }

  @Test
  public void mustProcessValidAtAtParameterForMatch() {
    String expectedQuery = "{'" + randomAlphabetic(5) + "':'" + randomAlphabetic(10) + "'}";
    Object[] args = {randomAlphabetic(5), expectedQuery};
    String query = AggregationType.MATCH.getValue(args, "@@1", (index, valueClass) -> (String) args[index]);
    assertEquals(query, expectedQuery);
  }

  // Resolves: https://github.com/krraghavan/mongodb-aggregate-query-support/issues/51
  @Test
  public void mustProcessValidAtAtParameterForProject() {
    String expectedQuery = "{'" + randomAlphabetic(5) + "':'" + randomAlphabetic(10) + "'}";
    Object[] args = {randomAlphabetic(5), expectedQuery};
    String query = AggregationType.PROJECT.getValue(args, "@@1", (index, valueClass) -> (String) args[index]);
    assertEquals(query, expectedQuery);
  }

  // Resolves: https://github.com/krraghavan/mongodb-aggregate-query-support/issues/51
  @Test
  public void mustProcessValidAtAtParameterForGroup() {
    String expectedQuery = "{'" + randomAlphabetic(5) + "':'" + randomAlphabetic(10) + "'}";
    Object[] args = {randomAlphabetic(5), expectedQuery};
    String query = AggregationType.GROUP.getValue(args, "@@1", (index, valueClass) -> (String) args[index]);
    assertEquals(query, expectedQuery);
  }

  @Test
  public void mustProcessValidAtAtParameterForEmptyMatch() {
    String expectedQuery = "{   }";
    Object[] args = {randomAlphabetic(5), expectedQuery};
    String query = AggregationType.MATCH.getValue(args, "@@1", (index, valueClass) -> (String) args[index]);
    assertNull(query);
  }

  @DataProvider
  public static Object[][] numericOnlyPhFixtures() {
    int integer = RandomUtils.nextInt();
    long longValue = RandomUtils.nextLong();
    String longStr = String.valueOf(longValue);
    String intStr = String.valueOf(integer);
    return new Object[][]{
        new Object[]{AggregationType.LIMIT, new Object[]{longValue}, longStr,
                     (BiFunction<Integer, Class<?>, String>) (v, c) -> longStr},
        new Object[]{AggregationType.LIMIT, new Object[]{integer}, intStr,
                     (BiFunction<Integer, Class<?>, String>) (v, c) -> intStr},
        new Object[]{AggregationType.SKIP, new Object[]{integer}, longStr,
                     (BiFunction<Integer, Class<?>, String>) (v, c) -> longStr},
        new Object[]{AggregationType.SKIP, new Object[]{integer}, intStr,
                     (BiFunction<Integer, Class<?>, String>) (v, c) -> intStr},
    };
  }


  @Test(dataProvider = "numericOnlyPhFixtures")
  public void mustProcessNumericOnlyPhValues(AggregationType aggregationType, Object [] args, String expectedResult,
                                             BiFunction<Integer, Class<?>, String> cbFn) {
    String query = AggregationType.getIntOrLongValueForQmarkPh(aggregationType, args, expectedResult, cbFn);
    assertEquals(query, expectedResult);
  }

  @Test(dataProvider = "invalidNumericOnlyPhFixtures", expectedExceptions = IllegalArgumentException.class)
  public void mustNotProcessNonNumericValuesOnNumericOnlyPhValues(AggregationType aggregationType, Object [] args) {
    String query = aggregationType.getValue(args, "?0", (index, valueClass) -> (String) args[index]);
  }
}