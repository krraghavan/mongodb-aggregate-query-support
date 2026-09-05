/*
 *  Copyright (c) 2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 */
package com.github.krr.mongodb.aggregate.support.utils;

import org.springframework.util.ClassUtils;

/**
 * Guards the 0.10.x artifacts against being run on a Spring Data MongoDb 4.x classpath.
 *
 * <p>Spring Data MongoDb 5.0 narrowed the return type of
 * {@code MongoRepositoryFactoryBean#getFactoryInstance} from {@code RepositoryFactorySupport} to
 * {@code MongoRepositoryFactory} (and likewise for the reactive variant).  The covariant overrides
 * in this library therefore compile to a different synthetic bridge method descriptor depending on
 * which Spring Data version they were built against.  Because the JVM dispatches virtual calls on
 * name <em>and</em> descriptor, a 0.10.x jar placed on a 4.x classpath has no method matching the
 * call site: Spring Data silently falls back to the stock factory and {@code @Aggregate} methods
 * stop being intercepted, with no exception and nothing in the log.
 *
 * <p>This check turns that silent regression into a startup failure.
 */
public final class SpringDataCompatibility {

  /**
   * Introduced in Spring Data MongoDb 5.0, absent from 4.x.
   */
  static final String SPRING_DATA_MONGODB_5_MARKER =
      "org.springframework.data.mongodb.core.query.DiskUse";

  private static final String INCOMPATIBLE_MESSAGE =
      "mongodb-aggregate-query-support 0.10.x requires Spring Data MongoDb 5.0 or later "
      + "(Spring Framework 7 / Spring Boot 4), but an earlier version is on the classpath - "
      + "could not load " + SPRING_DATA_MONGODB_5_MARKER + ". Aggregate query support would be "
      + "silently disabled on this classpath. Use the 0.9.x release train for Spring Data "
      + "MongoDb 4.x / Spring Framework 6.";

  private SpringDataCompatibility() {
  }

  /**
   * @throws IllegalStateException if the classpath has Spring Data MongoDb 4.x or earlier.
   */
  public static void assertSpringDataMongodb5OrLater() {
    assertSpringDataMongodb5OrLater(SpringDataCompatibility.class.getClassLoader());
  }

  static void assertSpringDataMongodb5OrLater(ClassLoader classLoader) {
    if (!ClassUtils.isPresent(SPRING_DATA_MONGODB_5_MARKER, classLoader)) {
      throw new IllegalStateException(INCOMPATIBLE_MESSAGE);
    }
  }
}
