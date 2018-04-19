package com.cisco.mongodb.aggregate.support.test.tests;

import com.cisco.mongodb.aggregate.support.test.beans.Possessions;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.PossessionsRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.cisco.mongodb.aggregate.support.test.utils.FixtureUtils.createPossessions;
import static org.testng.Assert.assertNotNull;

/**
 * camejavi 3/31/17
 */
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class UnbindableParametersTest extends AbstractTestNGSpringContextTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnbindableParametersTest.class);

  @Autowired
  private PossessionsRepository possessionsRepository;

  @Test
  public void mustBeAbleToUseConditionalPipelineStageAsWellAsPageable() {
    String tag = RandomStringUtils.randomAlphabetic(10);
    Possessions expectedCarPossessions = createPossessions(true, false, tag);
    Possessions expectedHomePossessions = createPossessions(false, true, tag);
    possessionsRepository.save(Arrays.asList(expectedCarPossessions, expectedHomePossessions));
    List<Possessions> carsOnlyPossessions = possessionsRepository.mutuallyExclusiveStagesPageable(
        tag, true, null, new PageRequest(0, 10));
    //this query must not throw error when accessing pageable in AggregateQueryProvider::getParameterValues
    assertNotNull(carsOnlyPossessions);
  }
}