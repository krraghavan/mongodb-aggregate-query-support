package com.github.krr.mongodb.aggregate.support.utils;

import static com.github.krr.mongodb.aggregate.support.utils.SpringDataCompatibility.SPRING_DATA_MONGODB_5_MARKER;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class SpringDataCompatibilityTest {

  /**
   * A loader that hides a single class, standing in for a Spring Data MongoDb 4.x classpath.
   */
  private static ClassLoader hiding(String hiddenClassName) {
    return new ClassLoader(SpringDataCompatibilityTest.class.getClassLoader()) {
      @Override
      protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (hiddenClassName.equals(name)) {
          throw new ClassNotFoundException(name);
        }
        return super.loadClass(name, resolve);
      }
    };
  }

  @Test
  void mustPassOnSpringDataMongodb5Classpath() {
    // the build itself resolves Spring Data MongoDb 5.x, so the marker must be present
    SpringDataCompatibility.assertSpringDataMongodb5OrLater();
  }

  @Test
  void mustFailFastOnSpringDataMongodb4Classpath() {
    ClassLoader springDataMongodb4 = hiding(SPRING_DATA_MONGODB_5_MARKER);
    IllegalStateException e = expectThrows(IllegalStateException.class,
                                           () -> SpringDataCompatibility.assertSpringDataMongodb5OrLater(
                                               springDataMongodb4));
    // the message has to tell the user which train to fall back to, otherwise the
    // fail-fast is no better than the silent failure it replaces
    assertTrue(e.getMessage().contains("0.9.x"), "Expecting the message to name the 0.9.x train");
    assertTrue(e.getMessage().contains(SPRING_DATA_MONGODB_5_MARKER),
               "Expecting the message to name the missing marker class");
  }

  @Test
  void mustNotBeFooledByAnUnrelatedMissingClass() {
    ClassLoader unrelated = hiding("com.example.NotOnTheClasspath");
    SpringDataCompatibility.assertSpringDataMongodb5OrLater(unrelated);
  }
}
