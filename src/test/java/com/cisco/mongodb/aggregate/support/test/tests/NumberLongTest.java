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

import com.cisco.mongodb.aggregate.support.test.beans.TestLongBean;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.TestLongRepository;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by rkolliva
 * 5/13/17.
 */
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class NumberLongTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestLongRepository testLongRepository;

  @Test
  public void mustReturnLongValueQueriedWithNumberLong() {
    TestLongBean longBean = new TestLongBean();
    long randomLong = RandomUtils.nextLong(System.nanoTime(), System.nanoTime() + 1000000);
    longBean.setRandomLong(randomLong);
    testLongRepository.save(longBean);
    TestLongBean actualBean = testLongRepository.getRandomLong(randomLong);
    Assert.assertNotNull(actualBean);

  }
}
