package com.github.krr.mongodb.aggregate.support.api;

import java.util.Collection;
import java.util.function.BiFunction;

public interface BoundParameterValue {

  /**
   * Returns the bound value of a parameter in a query string.  The return type is always
   * a string.  String parameter values are quoted in the returned value.
   *
   * @param methodParameterValues - the values of the method parameters to which the query is bound
   * @param query - the query string containing the placeholder being replaced.  Note that more than one
   *              placeholder may be present
   * @param valueProviderFn - A callback function that returns the String value at a specified index.
   *                        Implementations should cycle through all the placeholders and call this
   *                        function for each placeholders
   * @return - the query with the placeholders replaced with the value.
   */
  String getValue(Object[] methodParameterValues, String query, BiFunction<Integer, Class<?>, String> valueProviderFn);

  default boolean isString(Object[] methodParameterValues, int index) {
    validateIndex(methodParameterValues, index);
    return (methodParameterValues[index] instanceof String);
  }

  static void validateIndex(Object[] methodParameterValues, int index) {
    int length = methodParameterValues.length;
    if(index >= length) {
      throw new IllegalArgumentException("Index " + index + " is greater than number of parameters in method (" +
                                         length + ")");
    }
  }

  default Class<?> getValueClass(Object[] methodParameterValues, int index) {
    validateIndex(methodParameterValues, index);
    return methodParameterValues[index].getClass();
  }

  default boolean stringNeedsQuoting() {
    return true;
  }

  default boolean isArrayOrCollection(Object[] methodParameterValues, int index) {
    validateIndex(methodParameterValues, index);
    Class<?> valueClass = methodParameterValues[index].getClass();
    return Collection.class.isAssignableFrom(valueClass) || valueClass.isArray();
  }

}
