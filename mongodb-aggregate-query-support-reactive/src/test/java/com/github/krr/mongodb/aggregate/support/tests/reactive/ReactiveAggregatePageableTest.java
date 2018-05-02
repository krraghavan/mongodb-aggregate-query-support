package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactivePageableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * camejavi 3/15/17
 */
@SuppressWarnings("ConstantConditions")
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveAggregatePageableTest extends AbstractTestNGSpringContextTests {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ReactivePageableRepository pageableRepository;

  private int[] scoreArr = {70, 75, 80, 85, 90, 95};

  private final String[] SCORE_DOCS = {"{ \"_id\" : 1, \"subject\" : \"History\", \"score\" : " + scoreArr[0] + " }",
                                       "{ \"_id\" : 2, \"subject\" : \"History\", \"score\" : " + scoreArr[5] + " }",
                                       "{ \"_id\" : 3, \"subject\" : \"History\", \"score\" : " + scoreArr[3] + " }",
                                       "{ \"_id\" : 4, \"subject\" : \"History\", \"score\" : " + scoreArr[1] + " }",
                                       "{ \"_id\" : 5, \"subject\" : \"History\", \"score\" : " + scoreArr[2] + " }",
                                       "{ \"_id\" : 6, \"subject\" : \"History\", \"score\" : " + scoreArr[4] + " }",
                                       };

  @BeforeClass
  @SuppressWarnings("Duplicates")
  public void setup() {
    pageableRepository.deleteAll().block();
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
    pageableRepository.insert(scores).collectList().block();
  }

  @DataProvider
  public Object[][] pagingFixture() {
    return new Object[][]{
        new Object[]{0, 2},
        new Object[]{1, 2},
        new Object[]{2, 2},
        new Object[]{0, 3},
        new Object[]{1, 3}
    };
  }

  @Test
  public void checkPageableResult() {
    assertNotNull(pageableRepository, "Must have a repository");
    List<Score> scores = pageableRepository.findAll().collectList().block();
    assertNotNull(scores);
    assertEquals(scores.size(), SCORE_DOCS.length);
  }

  @Test(dataProvider = "pagingFixture")
  public void mustReturnCorrectPagesFromQueryWithPageable(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    List<Score> scoresAfterSkip = pageableRepository.getPageableScores(pageable).collectList().block();
//    List<Score> scoresAfterSkip = scoresAfterSkipDoc.getContent();
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
    Pageable pageable = PageRequest.of(2, scoresLen / 2);
    List<Score> scoresAfterSkip = pageableRepository.getPageableScores(pageable).collectList().block();
    assertEquals(scoresAfterSkip.size(), 0);
  }

  @Test(dataProvider = "pagingFixture")
  public void mustReturnPageEvenIfQueryHasAFacet(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    List<Score> scoresAfterSkipMono = pageableRepository.getPageableWithFacet(pageable).collectList().block();

    validateResults(page, size, pageable, scoresAfterSkipMono);
  }

  private void validateResults(int page, int size, Pageable pageable, List<Score> scores) {
    assertNotNull(scores);
    assertEquals(scores.size(), pageable.getPageSize());
    for (int i = 0; i < scores.size(); i++) {
      Score score = scores.get(i);
      //map the returned score in the page to the entry in scoreArr
      assertEquals(score.getScore(), scoreArr[page * size + i]);
    }
  }

}