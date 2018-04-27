package com.github.krr.mongodb.aggregate.support.utils;

/**
 * Created by rkolliva
 * 4/25/18.
 */

public class Assert {

  public static void notNull(Object object, String msg) {
    if(object == null) {
      throw new IllegalArgumentException(msg);
    }
  }

  @SuppressWarnings("unchecked")
  public static void isAssignable(Class superClass, Class subClass, String msg) {
    if(superClass.isAssignableFrom(subClass)) {
      return;
    }
    throw new IllegalArgumentException(msg);
  }
}
