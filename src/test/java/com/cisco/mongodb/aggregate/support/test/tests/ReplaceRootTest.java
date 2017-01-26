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

import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.TestReplaceRootRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 1/25/17.
 */


@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class ReplaceRootTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestReplaceRootRepository testReplaceRootRepository;

  @Test(enabled = false)
  public void mustAddFieldsToResults() {
    assertNotNull(testReplaceRootRepository);

    List<Map<String, Integer>> resultsBeanList = testReplaceRootRepository.getFruitInStockQuantity();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    final int[] orangeCount = new int[1];
    final int[] applesCount = new int[1];
    final int[] beetsCount = new int[1];
    final int[] yamsCount = {0};
    resultsBeanList.forEach((resultMap) -> resultMap.keySet().forEach((k) -> {
      if (k.equals("apples")) {
        applesCount[0] = resultMap.get("apples");
      }
      if (k.equals("oranges")) {
        orangeCount[0] = resultMap.get("oranges");
      }
      if (k.equals("beets")) {
        beetsCount[0] = resultMap.get("beets");
      }
      if (k.equals("yams")) {
        yamsCount[0] = resultMap.get("yams");
      }
    }));
    assertEquals(orangeCount[0], 20);
    assertEquals(applesCount[0], 60);
    assertEquals(beetsCount[0], 130);
    assertEquals(yamsCount[0], 200);
  }

  @Test(enabled = false)
  public void mustReplaceRootWithMatch() {
    assertNotNull(testReplaceRootRepository);

    List<Map<String, Integer>> resultsBeanList = testReplaceRootRepository.replaceRootWithMatch();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    final int[] index = {0};
    resultsBeanList.forEach((resultMap) -> {

      if (index[0] == 0) {
        assertEquals(Math.toIntExact(resultMap.get("cats")), 1);
        assertEquals(Math.toIntExact(resultMap.get("dogs")), 2);
      }
      else {
        assertEquals(Math.toIntExact(resultMap.get("cats")), 1);
        assertEquals(Math.toIntExact(resultMap.get("hamsters")), 3);
      }
      index[0]++;
    });
  }

  @SuppressWarnings("unchecked")
  @Test(enabled = false)
  public void mustReplaceRootWithExpression() {
    assertNotNull(testReplaceRootRepository);

    List<Map<String, String>> resultsBeanList = testReplaceRootRepository.replaceRootWithExpr();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 3);
    HashMap<String, Integer> expectedResultMap = new HashMap() {
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
      String fullName = resultMap.get("full_name");
      Integer count = expectedResultMap.get(fullName);
      expectedResultMap.put(fullName, ++count);
    });
    expectedResultMap.forEach((k, v) -> assertTrue(v == 1));
  }

  @SuppressWarnings("unchecked")
  @Test(enabled = false)
  public void mustReplaceRootWithArrays() {
    assertNotNull(testReplaceRootRepository);

    List<Map<String, String>> resultsBeanList = testReplaceRootRepository.replaceRootWithArray();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    HashMap<String, Integer> expectedResultMap = new HashMap() {
      {
        put("555-653-6527", 0);
      }

      {
        put("555-445-8767", 0);
      }
    };
    resultsBeanList.forEach((resultMap) -> {
      String fullName = resultMap.get("cell");
      Integer count = expectedResultMap.get(fullName);
      expectedResultMap.put(fullName, ++count);
    });
    expectedResultMap.forEach((k, v) -> assertTrue(v == 1));
  }
}
