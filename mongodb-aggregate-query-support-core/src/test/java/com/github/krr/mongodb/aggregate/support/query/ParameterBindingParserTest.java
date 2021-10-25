package com.github.krr.mongodb.aggregate.support.query;

import com.github.krr.mongodb.aggregate.support.query.AbstractAggregateQueryProvider.ParameterBinding;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class ParameterBindingParserTest {

  @DataProvider
  public static Object[][] singlePlaceholderFixture() {
    return new Object[][] {
        new Object[] {"{\"a\" : \"?2\"}", 2, true},
        new Object[] {"{\"a\" : \"?5\"}", 5, true},
        new Object[] {"{\"a\" : ?7}", 7, false},
    };
  }

  @DataProvider
  public static Object[][] multiplePlaceholdersFixture() {
    return new Object[][] {
        new Object[] {"{\"a\" : \"?2\", \"b\" : \"?5\"}", 2, true, 5, true},
        new Object[] {"{\"a\" : \"?5\", \"b\" : \"?6\"}", 5, true, 6, true},
        new Object[] {"{\"a\" : ?7}, \"b\" : \"?8\"", 7, false, 8, true},
        new Object[] {"{\"a\" : ?7}, \"b\" : ?8", 7, false, 8, false},
        };
  }

  @Test(dataProvider = "singlePlaceholderFixture")
  public void mustParseSinglePlaceholdersOnRHS(String input, int parameterIndex, boolean quoted) {
    ParameterBindingParser parser = new ParameterBindingParser() {
      @Override
      protected void bindDriverSpecificTypes(List<ParameterBinding> bindings, Object value) {
        throw new UnsupportedOperationException("Not supported");
      }
    };
    List<ParameterBinding> bindings = parser.parseParameterBindingsFrom(input);
    assertNotNull(bindings);
    assertEquals(bindings.size(), 1);
    validateBinding(parameterIndex, quoted, bindings.get(0));
  }

  private void validateBinding(int parameterIndex, boolean quoted, ParameterBinding binding) {
    assertEquals(binding.getParameterIndex(), parameterIndex);
    assertEquals(binding.isQuoted(), quoted);
  }

  @Test(dataProvider = "multiplePlaceholdersFixture")
  public void mustParseMultiplePlaceholdersOnRHS(String input,
                                                 int parameterIndex1, boolean quoted1,
                                                 int parameterIndex2, boolean quoted2) {
    ParameterBindingParser parser = new ParameterBindingParser() {
      @Override
      protected void bindDriverSpecificTypes(List<ParameterBinding> bindings, Object value) {
        throw new UnsupportedOperationException("Not supported");
      }
    };
    List<ParameterBinding> bindings = parser.parseParameterBindingsFrom(input);
    assertNotNull(bindings);
    assertEquals(bindings.size(), 2);
    ParameterBinding binding = bindings.get(0);
    validateBinding(parameterIndex1, quoted1, binding);
    binding = bindings.get(1);
    validateBinding(parameterIndex2, quoted2, binding);
  }

}