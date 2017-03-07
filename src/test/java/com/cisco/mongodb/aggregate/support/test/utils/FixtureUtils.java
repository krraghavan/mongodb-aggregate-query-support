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

package com.cisco.mongodb.aggregate.support.test.utils;

import com.cisco.mongodb.aggregate.support.test.beans.Asset;
import com.cisco.mongodb.aggregate.support.test.beans.Possessions;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cisco.mongodb.aggregate.support.test.utils.StringUtils.getRandomStrings;

/**
 * Created by rkolliva
 * 3/7/17.
 */


public class FixtureUtils {

  private FixtureUtils() {
    //
  }

  public static Possessions createPossessions(boolean cars, boolean homes) {
    return createPossessions(cars, homes, RandomStringUtils.randomAlphabetic(10));
  }

  public static Possessions createPossessions(boolean cars, boolean homes, String tag) {
    // add one document with only cars, another with only homes.
    Possessions possessions = new Possessions();
    possessions.setSortTestNumber(RandomUtils.nextLong(100, 400));
    possessions.setTag(tag);
    Asset carAssets = null;
    if(cars) {
      String id = RandomStringUtils.randomAlphabetic(10);
      possessions.setId(id);
      List<String> carAssetStrings = getRandomStrings();
      carAssets = new Asset();
      carAssets.setCars(carAssetStrings);
    }
    Asset homeAssets = null;
    if(homes) {
      String id1 = RandomStringUtils.randomAlphabetic(10);
      possessions.setId(id1);
      List<String> homeAssetStrings = getRandomStrings();
      homeAssets = new Asset();
      homeAssets.setHomes(homeAssetStrings);
    }
    if(cars && homes) {
      possessions.setAssets(Arrays.asList(carAssets, homeAssets));
    }
    else if(cars) {
      possessions.setAssets(Collections.singletonList(carAssets));
    }
    else if(homes){
      possessions.setAssets(Collections.singletonList(homeAssets));
    }
    return possessions;
  }


  public static List<Possessions> createPossessionsWithSortField(String tag) {
    int count = RandomUtils.nextInt(5, 10);
    List<Possessions> retval = new ArrayList<>();
    for(int i = 0; i < count; i++) {
      retval.add(createPossessions(true, true, tag));
    }
    return retval;
  }
}
