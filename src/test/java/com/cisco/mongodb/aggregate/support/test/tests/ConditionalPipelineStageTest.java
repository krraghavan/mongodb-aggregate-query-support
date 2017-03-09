/*
 *  Copyright (c) 2017 the original author or authors.
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

package com.cisco.mongodb.aggregate.support.test.tests;

import com.cisco.mongodb.aggregate.support.test.beans.Possessions;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.PossessionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.cisco.mongodb.aggregate.support.test.utils.FixtureUtils.createPossessions;
import static com.cisco.mongodb.aggregate.support.test.utils.FixtureUtils.createPossessionsWithSortField;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by rkolliva
 * 3/8/17.
 */

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class ConditionalPipelineStageTest extends AbstractTestNGSpringContextTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalPipelineStageTest.class);

  @Autowired
  private PossessionsRepository possessionsRepository;

  @Test
  public void mustConditionallyExcludePipelineStagesMutuallyExclusiveCase() {
    String tag = "mustConditionallyExcludePipelineStagesMutuallyExclusiveCase";
    Possessions expectedCarPossessions = createPossessions(true, false, tag);
    Possessions expectedHomePossessions = createPossessions(false, true, tag);
    possessionsRepository.save(Arrays.asList(expectedCarPossessions, expectedHomePossessions));
    List<Possessions> carsOnlyPossessions = possessionsRepository.mutuallyExclusiveStages(tag, true,
                                                                                          null);
    assertTrue(carsOnlyPossessions != null);
    assertTrue(carsOnlyPossessions.get(0).getAssets().get(0).getCars() != null);
    assertTrue(carsOnlyPossessions.get(0).getAssets().get(0).getHomes() == null);
    assertEquals(carsOnlyPossessions.get(0).getId(), expectedCarPossessions.getId());

    List<Possessions> homesOnlyPossessions = possessionsRepository.mutuallyExclusiveStages(tag, null,
                                                                                           true);
    assertTrue(homesOnlyPossessions != null);
    assertTrue(homesOnlyPossessions.get(0).getAssets().get(0).getCars() == null);
    assertTrue(homesOnlyPossessions.get(0).getAssets().get(0).getHomes() != null);
    assertEquals(homesOnlyPossessions.get(0).getId(), expectedHomePossessions.getId());
  }

  @Test
  public void mustReturnResultWithMixOfConditionalAndUnconditionalStages() {
    String tag = "mustReturnResultWithMixOfConditionalAndUnconditionalStages";
    List<Possessions> expectedCarPossessions = createPossessionsWithSortField(tag, false, true);
    List<Possessions> expectedHomePossessions = createPossessionsWithSortField(tag, true, false);
    possessionsRepository.save(expectedCarPossessions);
    possessionsRepository.save(expectedHomePossessions);

    List<Possessions> carsOnlyPossessions = possessionsRepository
        .getWithMixOfConditionalAndUnconditionalStages(tag, true,
                                                       null);
    assertTrue(carsOnlyPossessions != null);
    LOGGER.info("ExpectedPosessions ListIds:{}", expectedCarPossessions);

    verifyPossessions(expectedCarPossessions, carsOnlyPossessions);

    List<Possessions> homesOnlyPossessions = possessionsRepository.getWithMixOfConditionalAndUnconditionalStages(tag,
                                                                                                                 null,
                                                                                                                 true);

    verifyPossessions(expectedHomePossessions, homesOnlyPossessions);
  }

  @Test
  public void mustReturnResultWithPotentiallyNullIdList() {
    String tag = "mustReturnResultWithPotentiallyNullIdList";
    List<Possessions> expectedCarPossessions = createPossessionsWithSortField(tag, false, true);
    List<Possessions> expectedHomePossessions = createPossessionsWithSortField(tag, true, false);
    possessionsRepository.save(expectedCarPossessions);
    possessionsRepository.save(expectedHomePossessions);

    List<String> carIds = new ArrayList<>();
    expectedCarPossessions.forEach(expectedCarPossession -> carIds.add(expectedCarPossession.getId()));

    List<Possessions> expectedAllPossessions = new ArrayList<>();
    expectedAllPossessions.addAll(expectedCarPossessions);
    expectedAllPossessions.addAll(expectedHomePossessions);

    List<Possessions> allPossessions = possessionsRepository
            .getPossessionsWithPotentiallyNullIdList(tag,null);
    assertTrue(allPossessions != null);
    LOGGER.info("allPossessions:{}", allPossessions);

    verifyPossessions(expectedAllPossessions, allPossessions);

    List<Possessions> carPossessions = possessionsRepository
            .getPossessionsWithPotentiallyNullIdList(tag, carIds);
    assertTrue(carPossessions != null);
    LOGGER.info("carPossessions:{}", carPossessions);

    verifyPossessions(expectedCarPossessions, carPossessions);
  }

  private void verifyPossessions(List<Possessions> expectedPossessions, List<Possessions> actualPossessions) {
    List<String> possessionsList = expectedPossessions.stream().map(Possessions::getId).collect(Collectors.toList());
    final long[] lastNumber = {999999};
    // verify sort order
    LOGGER.info("ExpectedPosessions ListIds:{}", possessionsList);
    actualPossessions.forEach(possession -> {
      Long sortTestNumber = possession.getSortTestNumber();
      assertTrue(sortTestNumber <= lastNumber[0]);
      lastNumber[0] = sortTestNumber;
      LOGGER.info("Possession id:{}", possession.getId());
      assertTrue(possessionsList.contains(possession.getId()));
    });
  }

}
