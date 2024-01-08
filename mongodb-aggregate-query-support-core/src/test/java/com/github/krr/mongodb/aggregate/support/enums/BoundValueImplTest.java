package com.github.krr.mongodb.aggregate.support.enums;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testng.Assert.assertEquals;

public class BoundValueImplTest {

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
                   "   \"tag\": '@0'" +
                   "}", new Object[] {randomValues2}, "{" +
                        "   \"tag\": '" + randomValues2 + "'" +
                        "}", true},
      // only @@ placeholder
      new Object[]{"@@2", new Object[] {randomValues1, randomValues2, randomValues3}, randomValues3, false},
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
  public void mustProcessValidAtAtParameter() {
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
}