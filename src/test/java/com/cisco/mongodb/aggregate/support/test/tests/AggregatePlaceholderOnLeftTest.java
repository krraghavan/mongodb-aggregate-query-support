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

import com.cisco.mongodb.aggregate.support.test.beans.Asset;
import com.cisco.mongodb.aggregate.support.test.beans.Possessions;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.PossessionsRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by rkolliva
 * 3/1/17.
 */


@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregatePlaceholderOnLeftTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private PossessionsRepository possessionsRepository;

  private Possessions possessionsWithCars;

  private Possessions possessionsWithHomes;

  @BeforeClass
  public void setup() {
    // add one document with only cars, another with only homes.
    possessionsWithCars = new Possessions();
    String id = RandomStringUtils.randomAlphabetic(10);
    possessionsWithCars.setId(id);
    List<String> carAssets = getRandomStrings();
    Asset asset = new Asset();
    asset.setCars(carAssets);
    possessionsWithCars.setAssets(Collections.singletonList(asset));

    possessionsWithHomes = new Possessions();
    String id1 = RandomStringUtils.randomAlphabetic(10);
    possessionsWithHomes.setId(id1);
    List<String> homeAssets = getRandomStrings();
    Asset asset1 = new Asset();
    asset1.setHomes(homeAssets);
    possessionsWithHomes.setAssets(Collections.singletonList(asset1));

    possessionsRepository.save(Arrays.asList(possessionsWithCars, possessionsWithHomes));

  }

  private List<String> getRandomStrings() {
    int count = RandomUtils.nextInt(1, 10);
    List<String> cars = new ArrayList<>();
    for(int i = 0; i < count; i++) {
      cars.add(RandomStringUtils.randomAlphabetic(10));
    }
    return cars;
  }

  @Test
  public void mustReplacePlaceholderOnLeftForAggregateQuery() {
    boolean hasCars = possessionsRepository.hasCars();
    assertTrue(hasCars);
  }

  @Test(dataProvider = "possessionsProvider")
  public void mustReturnCarsFromAggregateQuery(String type, Possessions expectedPossessions) {
    List<Possessions> possessions = possessionsRepository.getPossessions(type);
    assertNotNull(possessions);
    assertTrue(possessions.size() == 1);
    Possessions possession = possessions.get(0);
    assertTrue(possession.getId().equals(expectedPossessions.getId()));
  }

  @DataProvider
  public Object[][] possessionsProvider() {
    return new Object[][] {
        new Object[] {"cars", possessionsWithCars},
        new Object[] {"homes", possessionsWithHomes}
    };
  }

}
