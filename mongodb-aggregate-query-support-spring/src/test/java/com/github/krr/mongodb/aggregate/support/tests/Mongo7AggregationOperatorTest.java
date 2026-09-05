package com.github.krr.mongodb.aggregate.support.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import com.github.krr.mongodb.aggregate.support.config.MongoDBTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.Mongo7OperatorRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Pins the claim that MongoDb 7.0 accumulators need no library support: they are part of the stage
 * body, which is handed to the driver verbatim.  Skipped on older servers because the operators
 * themselves do not exist there.
 */
@ContextConfiguration(classes = MongoDBTestConfiguration.class)
public class Mongo7AggregationOperatorTest extends AbstractTestNGSpringContextTests {

  private static final String[] SCORE_DOCS = {
      "{ \"_id\" : 1, \"subject\" : \"History\", \"score\" : 88 }",
      "{ \"_id\" : 2, \"subject\" : \"History\", \"score\" : 92 }",
      "{ \"_id\" : 3, \"subject\" : \"History\", \"score\" : 97 }",
      "{ \"_id\" : 4, \"subject\" : \"History\", \"score\" : 71 }",
      "{ \"_id\" : 5, \"subject\" : \"History\", \"score\" : 79 }"};

  @Autowired
  private Mongo7OperatorRepository repository;

  private static void requireMongo7() {
    String version = System.getProperty("mongoVersion");
    if (version == null || version.compareTo("7.") < 0) {
      throw new SkipException("$median/$percentile need MongoDb 7.0+, running against " + version);
    }
  }

  @BeforeClass
  public void setup() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Score> scores = new ArrayList<>();
    for (String doc : Arrays.asList(SCORE_DOCS)) {
      scores.add(mapper.readValue(doc, Score.class));
    }
    repository.deleteAll();
    repository.insert(scores);
  }

  @Test
  public void mustSupportMedianAccumulatorWithoutLibraryChanges() {
    requireMongo7();
    Document result = repository.medianScore();
    assertNotNull(result, "Expecting a result from the $median pipeline");
    assertEquals(((Number) result.get("medianScore")).intValue(), 88);
  }

  @Test
  public void mustSupportPercentileAccumulatorWithoutLibraryChanges() {
    requireMongo7();
    Document result = repository.scorePercentiles();
    assertNotNull(result, "Expecting a result from the $percentile pipeline");
    List<?> percentiles = (List<?>) result.get("scorePercentiles");
    assertNotNull(percentiles, "Expecting the $percentile accumulator to return an array");
    assertEquals(percentiles.size(), 2, "Expecting one value per requested percentile");
  }
}
