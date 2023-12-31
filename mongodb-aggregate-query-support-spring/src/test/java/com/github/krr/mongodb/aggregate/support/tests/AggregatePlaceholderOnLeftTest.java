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

package com.github.krr.mongodb.aggregate.support.tests;

import com.github.krr.mongodb.aggregate.support.beans.Possessions;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.PossessionsRepository;
import com.github.krr.mongodb.aggregate.support.utils.FixtureUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 3/1/17.
 */

@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "Duplicates"})
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregatePlaceholderOnLeftTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private PossessionsRepository possessionsRepository;

  @Test
  public void mustReplacePlaceholderOnLeftForAggregateQuery() {
    String tag = "mustReplacePlaceholderOnLeftForAggregateQuery";
    Possessions expectedPossessions = FixtureUtils.createPossessions(true, false, tag);
    possessionsRepository.save(expectedPossessions);
    boolean hasCars = possessionsRepository.hasCars(tag);
    assertTrue(hasCars);
  }

  @Test(dataProvider = "possessionsProvider")
  public void mustReturnCarsFromAggregateQuery(String tag, boolean cars, boolean homes) {
    Possessions expectedPossessions = FixtureUtils.createPossessions(cars, homes, tag);
    possessionsRepository.save(expectedPossessions);

    List<Possessions> possessions = possessionsRepository.getPossessions(tag);
    assertNotNull(possessions);
    assertEquals(possessions.size(), 1);
    Possessions possession = possessions.get(0);
    assertEquals(expectedPossessions.getId(), possession.getId());
  }

  @Test
  public void mustReplaceSortParameterWithPlaceholder() {
    String tag = "mustReplaceSortParameterWithPlaceholder";
    List<Possessions> possessionss = FixtureUtils.createPossessionsWithSortField(tag);
    possessionsRepository.saveAll(possessionss);
    String sortString = "{sortTestNumber:-1}";
    List<Possessions> possessions = possessionsRepository.getPossesionsSortedByTag(tag, sortString);
    assertNotNull(possessions);
    assertEquals(possessionss.size(), possessions.size());
    // verify that Possessions are in descending order.
    final long[] lastNumber = {999999};
    possessions.forEach(i -> {
      Long sortTestNumber = i.getSortTestNumber();
      assertTrue(sortTestNumber <= lastNumber[0]);
      lastNumber[0] = sortTestNumber;
    });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionIfAtAtParameterIsOutOfBounds() {
    possessionsRepository.getPossessionsInvalidSortParameter("xxx", "xxx");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionIfAtAtParameterIsNotAString() {
    possessionsRepository.getPossessionsInvalidSortParameter("xxx", 123);
  }

  @DataProvider
  public Object[][] possessionsProvider() {
    return new Object[][] {
        new Object[] {"possessionsProviderCars", true, false},
        new Object[] {"possessionsProviderHomes", false, true}
    };
  }

}
