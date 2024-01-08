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

package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.github.krr.mongodb.aggregate.support.beans.Possessions;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactivePossessionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.github.krr.mongodb.aggregate.support.utils.FixtureUtils.createPossessions;
import static com.github.krr.mongodb.aggregate.support.utils.FixtureUtils.createPossessionsWithSortField;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by rkolliva
 * 3/1/17.
 */

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveAggregatePlaceholderOnLeftTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private ReactivePossessionsRepository possessionsRepository;

  @Test
  public void mustReplacePlaceholderOnLeftForAggregateQuery() {
    String tag = "mustReplacePlaceholderOnLeftForAggregateQuery";
    Possessions expectedPossessions = createPossessions(true, false, tag);
    possessionsRepository.save(expectedPossessions).block();
    boolean hasCars = possessionsRepository.hasCars(tag);
    assertTrue(hasCars);
  }

  @Test(dataProvider = "possessionsProvider")
  public void mustReturnCarsFromAggregateQuery(String tag, boolean cars, boolean homes) {
    Possessions expectedPossessions = createPossessions(cars, homes, tag);
    possessionsRepository.save(expectedPossessions).block();

    List<Possessions> possessions = possessionsRepository.getPossessions(tag).collectList().block();
    assertNotNull(possessions);
    assertTrue(possessions.size() == 1);
    Possessions possession = possessions.get(0);
    assertTrue(possession.getId().equals(expectedPossessions.getId()));
  }

  @Test
  public void mustReplaceSortParameterWithPlaceholder() {
    String tag = "mustReplaceSortParameterWithPlaceholder";
    List<Possessions> possessionss = createPossessionsWithSortField(tag);
    possessionsRepository.saveAll(possessionss).collectList().block();
    String sortString = "{sortTestNumber:-1}";
    List<Possessions> possessions = possessionsRepository.getPossesionsSortedByTag(tag, sortString).collectList()
                                                         .block();
    assertNotNull(possessions);
    assertTrue(possessions.size() == possessionss.size());
    // verify that Possessions are in descending order.
    final long[] lastNumber = {999999};
    possessions.forEach(i -> {
      Long sortTestNumber = i.getSortTestNumber();
      assertTrue(sortTestNumber <= lastNumber[0]);
      lastNumber[0] = sortTestNumber;
    });
  }
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionIfSortParameterPlaceholderHasQuotes() {
    String tag = "mustReplaceSortParameterWithPlaceholder";
    List<Possessions> possessionss = createPossessionsWithSortField(tag);
    possessionsRepository.saveAll(possessionss).collectList().block();
    String sortString = "{sortTestNumber:-1}";
    possessionsRepository.getPossesionsSortedByTagInvalidSortPlaceholder(tag, sortString).collectList().block();
  }

  @DataProvider
  public Object[][] possessionsProvider() {
    return new Object[][] {
        new Object[] {"possessionsProviderCars", true, false},
        new Object[] {"possessionsProviderHomes", false, true}
    };
  }

}
