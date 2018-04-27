package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveSkipRepository;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

/**
 * camejavi 3/15/17
 */
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveAggregateSkipTest extends AbstractTestNGSpringContextTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveAggregateSkipTest.class);

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ReactiveSkipRepository skipRepository;

  private final String[] SCORE_DOCS = {"{ \"id\" : 1, \"subject\" : \"History\", \"score\" : 88 }",
                                       "{ \"id\" : 2, \"subject\" : \"History\", \"score\" : 92 }",
                                       "{ \"id\" : 3, \"subject\" : \"History\", \"score\" : 97 }",
                                       "{ \"id\" : 4, \"subject\" : \"History\", \"score\" : 71 }",
                                       "{ \"id\" : 5, \"subject\" : \"History\", \"score\" : 79 }",
                                       "{ \"id\" : 6, \"subject\" : \"History\", \"score\" : 83 }"};

  @BeforeClass
  @SuppressWarnings("Duplicates")
  public void setup() {
    skipRepository.deleteAll().block();
    ObjectMapper mapper = new ObjectMapper();
    List<Score> scores = new ArrayList<>();
    AtomicInteger cnt = new AtomicInteger();
    Arrays.asList(SCORE_DOCS).forEach((s) -> {
      try {
        Score scoreObj = mapper.readValue(s, Score.class);
        Random random = new Random(System.currentTimeMillis());
        int startInclusive = Math.abs(random.nextInt());
        scoreObj.setId(RandomUtils.nextInt(startInclusive, startInclusive + 10000));
        scores.add(scoreObj);
        cnt.getAndIncrement();
      }
      catch (IOException e) {
        assertTrue(false, e.getMessage());
      }
    });
    LOGGER.error("Skip repository contains {} scores before adding {}", skipRepository.count().block(), cnt.get());
    skipRepository.insert(scores).collectList().block();
    LOGGER.error("Skip repository contains {} scores after adding {}", skipRepository.count().block(), scores.size());
  }

  @Test
  public void mustSkipDocumentsFromRepository() {
    LOGGER.error("Skip repository contains {} scores in test method", skipRepository.count().block());

    assertNotNull(skipRepository, "Must have a repository");
    List<Score> scores = skipRepository.findAll().collectList().block();
    assertNotNull(scores);
    assertEquals(scores.size(), SCORE_DOCS.length);
    List scoresAfterSkip = skipRepository.getScoresAfterSkip().collectList().block();
    assertNotNull(scoresAfterSkip);
    assertEquals(scoresAfterSkip.size(), (SCORE_DOCS.length - 3));
  }

}