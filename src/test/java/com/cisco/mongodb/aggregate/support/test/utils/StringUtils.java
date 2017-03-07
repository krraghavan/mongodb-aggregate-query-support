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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkolliva
 * 3/7/17.
 */


public class StringUtils {

  private StringUtils() {
  }

  public static List<String> getRandomStrings() {
    int count = RandomUtils.nextInt(1, 10);
    List<String> cars = new ArrayList<>();
    for(int i = 0; i < count; i++) {
      cars.add(RandomStringUtils.randomAlphabetic(10));
    }
    return cars;
  }


}
