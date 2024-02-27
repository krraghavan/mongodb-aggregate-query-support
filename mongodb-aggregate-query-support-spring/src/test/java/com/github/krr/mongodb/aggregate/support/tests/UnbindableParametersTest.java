package com.github.krr.mongodb.aggregate.support.tests;

import com.github.krr.mongodb.aggregate.support.beans.Possessions;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.PossessionsRepository;
import com.github.krr.mongodb.aggregate.support.utils.FixtureUtils;
import com.google.common.truth.Truth;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * camejavi 3/31/17
 */
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class UnbindableParametersTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private PossessionsRepository possessionsRepository;

  @DataProvider
  public Object[][] keyPlaceholders() {
    String tag = RandomStringUtils.randomAlphabetic(10);
    Possessions expectedCarPossessions = FixtureUtils.createPossessions(true, false, tag);
    Possessions expectedHomePossessions = FixtureUtils.createPossessions(false, true, tag);
    possessionsRepository.saveAll(Arrays.asList(expectedCarPossessions, expectedHomePossessions));
    return new Object[][] {
        {expectedCarPossessions, tag, "cars"},
        {expectedHomePossessions, tag, "homes"},
        };
  }

  @Test
  public void mustBeAbleToUseConditionalPipelineStageAsWellAsPageable() {
    String tag = RandomStringUtils.randomAlphabetic(10);
    Possessions expectedCarPossessions = FixtureUtils.createPossessions(true, false, tag);
    Possessions expectedHomePossessions = FixtureUtils.createPossessions(false, true, tag);
    possessionsRepository.saveAll(Arrays.asList(expectedCarPossessions, expectedHomePossessions));
    List<Possessions> carsOnlyPossessions = possessionsRepository.mutuallyExclusiveStagesPageable(
        tag, true, null, PageRequest.of(0, 10));
    //this query must not throw error when accessing pageable in AggregateQueryProvider::getParameterValues
    Truth.assertThat(carsOnlyPossessions).isNotNull();
  }

  @Test(dataProvider = "keyPlaceholders")
  public void mustReturnResultsForKeyPlaceholder(Possessions possessions, String tag, String key) {
    List<Possessions> actualPossessions = possessionsRepository.placeholderOnKeys(tag,
                                                                                  key,
                                                                                  PageRequest.of(0,
                                                                                                 10));
    //this query must not throw error when accessing pageable in AggregateQueryProvider::getParameterValues
    assertThat(actualPossessions).hasSize(1);
    assertThat(actualPossessions.get(0)).isEqualTo(possessions);
  }

}