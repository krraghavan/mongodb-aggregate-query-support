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

import com.cisco.mongodb.aggregate.support.test.beans.Histogram;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.TestBucketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 1/21/17.
 */

@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateBucketTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestBucketRepository testBucketRepository;

  /**
   * This test is disabled because it needs to be run on a real mongo instance
   * Fongo does not yet support the $bucket operator.
   */
  @Test(enabled = false)
  public void mustReturnBucketsFromRepository() {
    assertNotNull(testBucketRepository, "Must have a repository");
    List<Histogram> histogramList = testBucketRepository.fixedBucket();
    assertNotNull(histogramList);
    assertEquals(histogramList.size(), 3);
    Map<String, Histogram> expectedResultMap = new HashMap<>();
    expectedResultMap.put("0", new Histogram("0", new ArrayList<String>(){{add("Fusion");}}, 1));
    expectedResultMap.put("60", new Histogram("60", new ArrayList<String>(){{add("Mustang");}}, 1));
    expectedResultMap.put("moreThan75", new Histogram("moreThan75", new ArrayList<String>(){{add("Volt");}}, 1));

    histogramList.forEach((item) -> assertTrue(item.equals(expectedResultMap.get(item.getId()))));
  }

}
