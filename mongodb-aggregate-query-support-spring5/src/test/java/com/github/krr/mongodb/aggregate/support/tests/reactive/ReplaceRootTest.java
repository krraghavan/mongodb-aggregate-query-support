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

import com.github.krr.mongodb.aggregate.support.beans.TestReplaceRootBean;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.fixtures.AggregateQueryFixtures;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveTestReplaceRootRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 1/25/17.
 */
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReplaceRootTest extends AbstractTestNGSpringContextTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceRootTest.class);

  @Autowired
  private ReactiveTestReplaceRootRepository testReplaceRootRepository;

  @BeforeClass
  private void setupRepository() throws IOException {
    List<TestReplaceRootBean> replaceRootBeans = AggregateQueryFixtures.newReplaceRootFixture();
    testReplaceRootRepository.saveAll(replaceRootBeans).collectList().block();
  }

  @Test
  public void mustAddFieldsToResults() {
    assertNotNull(testReplaceRootRepository);
    LOGGER.error("testReplaceRootRepository contains {} documents in test method", testReplaceRootRepository.count().block());

    List<Document> resultsBeanList = testReplaceRootRepository.getFruitInStockQuantity().collectList().block();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    final int[] orangeCount = new int[1];
    final int[] applesCount = new int[1];
    final int[] beetsCount = new int[1];
    final int[] yamsCount = {0};
    resultsBeanList.forEach((resultMap) -> {
      LOGGER.error("Class:{}, value:{}", resultMap.getClass(), resultMap);
      resultMap.keySet().forEach((k) -> {
        if (k.equals("apples")) {
          applesCount[0] = (int) resultMap.get("apples");
        }
        if (k.equals("oranges")) {
          orangeCount[0] = (int) resultMap.get("oranges");
        }
        if (k.equals("beets")) {
          beetsCount[0] = (int) resultMap.get("beets");
        }
        if (k.equals("yams")) {
          yamsCount[0] = (int) resultMap.get("yams");
        }
      });
    });
    assertEquals(orangeCount[0], 20);
    assertEquals(applesCount[0], 60);
    assertEquals(beetsCount[0], 130);
    assertEquals(yamsCount[0], 200);
  }

  @Test
  public void mustReplaceRootWithExpression() {
    assertNotNull(testReplaceRootRepository);

    LOGGER.error("testReplaceRootRepository contains {} documents in test method", testReplaceRootRepository.count().block());
    List<Document> resultsBeanList = testReplaceRootRepository.replaceRootWithExpr().collectList().block();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 3);
    Map<String, Integer> expectedResultMap = new HashMap<String, Integer>() {
      {
        put("Gary Sheffield", 0);
      }

      {
        put("Nancy Walker", 0);
      }

      {
        put("Peter Sumner", 0);
      }
    };
    resultsBeanList.forEach((resultMap) -> {
      String fullName = (String) resultMap.get("fullName");
      Integer count = expectedResultMap.get(fullName);
      expectedResultMap.put(fullName, ++count);
    });
    expectedResultMap.forEach((k, v) -> assertEquals((int) v, 1));
  }

  @Test
  public void mustReplaceRootWithArrays() {
    assertNotNull(testReplaceRootRepository);
    LOGGER.error("testReplaceRootRepository contains {} documents in test method", testReplaceRootRepository.count().block());

    List<Document> resultsBeanList = testReplaceRootRepository.replaceRootWithArray().collectList().block();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    Map<String, Integer> expectedResultMap = new HashMap<String, Integer>() {
      {
        put("555-653-6527", 0);
      }

      {
        put("555-445-8767", 0);
      }
    };
    resultsBeanList.forEach((resultMap) -> {
      String fullName = (String) resultMap.get("cell");
      Integer count = expectedResultMap.get(fullName);
      expectedResultMap.put(fullName, ++count);
    });
    expectedResultMap.forEach((k, v) -> assertEquals((int) v, 1));
  }
}
