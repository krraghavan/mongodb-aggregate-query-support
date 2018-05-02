package com.github.krr.mongodb.aggregate.support.tests.nonreactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import com.github.krr.mongodb.aggregate.support.config.NonReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.nonreactive.SkipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * camejavi 3/15/17
 */
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "Duplicates"})
@ContextConfiguration(classes = NonReactiveAggregateTestConfiguration.class)
public class AggregateSkipTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private SkipRepository skipRepository;

  private final String[] SCORE_DOCS = {"{ \"_id\" : 1, \"subject\" : \"History\", \"score\" : 88 }",
                                       "{ \"_id\" : 2, \"subject\" : \"History\", \"score\" : 92 }",
                                       "{ \"_id\" : 3, \"subject\" : \"History\", \"score\" : 97 }",
                                       "{ \"_id\" : 4, \"subject\" : \"History\", \"score\" : 71 }",
                                       "{ \"_id\" : 5, \"subject\" : \"History\", \"score\" : 79 }",
                                       "{ \"_id\" : 6, \"subject\" : \"History\", \"score\" : 83 }"};

  @BeforeClass
  @SuppressWarnings("Duplicates")
  public void setup() {
    skipRepository.deleteAll();
    ObjectMapper mapper = new ObjectMapper();
    List<Score> scores = new ArrayList<>();
    Arrays.asList(SCORE_DOCS).forEach((s) -> {
      try {
        scores.add(mapper.readValue(s, Score.class));
      }
      catch (IOException e) {
        assertTrue(false, e.getMessage());
      }
    });
    skipRepository.insert(scores);
  }

  @Test
  public void mustSkipDocumentsFromRepository() {
    assertNotNull(skipRepository, "Must have a repository");
    List<Score> scores = skipRepository.findAll();
    assertNotNull(scores);
    assertEquals(scores.size(), SCORE_DOCS.length);
    List scoresAfterSkip = skipRepository.getScoresAfterSkip();
    assertNotNull(scoresAfterSkip);
    assertEquals(scoresAfterSkip.size(), (SCORE_DOCS.length - 3));
  }

}