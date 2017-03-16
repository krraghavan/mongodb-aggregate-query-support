package com.cisco.mongodb.aggregate.support.test.tests;

import com.cisco.mongodb.aggregate.support.test.beans.Score;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.PageableRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregatePageableTest extends AbstractTestNGSpringContextTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregatePageableTest.class);

  @Autowired
  private PageableRepository pageableRepository;

  int[] scoreArr = {70, 75, 80, 85, 90, 95};

  private final String[] SCORE_DOCS = {"{ \"id\" : 1, \"subject\" : \"History\", \"score\" : " + scoreArr[0] + " }",
                                       "{ \"id\" : 2, \"subject\" : \"History\", \"score\" : " + scoreArr[5] + " }",
                                       "{ \"id\" : 3, \"subject\" : \"History\", \"score\" : " + scoreArr[3] + " }",
                                       "{ \"id\" : 4, \"subject\" : \"History\", \"score\" : " + scoreArr[1] + " }",
                                       "{ \"id\" : 5, \"subject\" : \"History\", \"score\" : " + scoreArr[2] + " }",
                                       "{ \"id\" : 6, \"subject\" : \"History\", \"score\" : " + scoreArr[4] + " }",
                                       };

  @BeforeClass
  @SuppressWarnings("Duplicates")
  public void setup() throws Exception {
    pageableRepository.deleteAll();
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
    pageableRepository.insert(scores);
  }

  @Test
  public void mustReturnCorrectPagesAfterSortingDocuments() {
    checkPageableResult(0, 2);
    checkPageableResult(1, 2);
    checkPageableResult(2, 2);
    checkPageableResult(0, 3);
    checkPageableResult(1, 3);
  }

  public void checkPageableResult(int page, int size) {
    assertNotNull(pageableRepository, "Must have a repository");
    List<Score> scores = pageableRepository.findAll();
    assertNotNull(scores);
    assertEquals(scores.size(), SCORE_DOCS.length);
    Pageable pageable = new PageRequest(page, size);
    List scoresAfterSkip = pageableRepository.getPageableScores(pageable);

    assertNotNull(scoresAfterSkip);
    assertEquals(scoresAfterSkip.size(), pageable.getPageSize());
    for (int i = 0; i < scoresAfterSkip.size(); i++) {
      Score score = (Score) scoresAfterSkip.get(i);
      //map the returned score in the page to the entry in scoreArr
      assertEquals(score.getScore(), scoreArr[page * size + i]);
    }
  }

  @Test
  public void mustNotThrowErrorWhenPageIsOutOfBounds() {
    int scoresLen = SCORE_DOCS.length;
    Pageable pageable = new PageRequest(2, scoresLen / 2);
    List scoresAfterSkip = pageableRepository.getPageableScores(pageable);
    assertNull(scoresAfterSkip);
  }

}