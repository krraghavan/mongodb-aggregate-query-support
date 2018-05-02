/*
 *  Copyright (c) 2016 the original author or authors.
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

import com.github.krr.mongodb.aggregate.support.repository.TestForeignKeyRepository;
import com.github.krr.mongodb.aggregate.support.repository.TestPrimaryKeyRepository;
import com.github.krr.mongodb.aggregate.support.beans.TestPrimaryKeyBean;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.beans.TestForeignKeyBean;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * @author rkolliva.
 */
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "Duplicates"})
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateLookupTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestPrimaryKeyRepository testPrimaryKeyRepository;

  @Autowired
  private TestForeignKeyRepository testForeignKeyRepository;

  private int foreignKeyItemsCount;

  private List<String> foreignKeys = new ArrayList<>();

  @BeforeClass
  private void setup() {
    String primaryKey = RandomStringUtils.randomAlphabetic(10);

    // create a single primarykey record
    TestPrimaryKeyBean primaryKeyBean = new TestPrimaryKeyBean();
    primaryKeyBean.setRandomPrimaryKey(primaryKey);
    testPrimaryKeyRepository.save(primaryKeyBean);

    // generate a random number of foreign key items
    foreignKeyItemsCount = RandomUtils.nextInt(10, 40);
    List<TestForeignKeyBean> foreignKeyBeans = new ArrayList<>(foreignKeyItemsCount);
    for (int i = 0; i < foreignKeyItemsCount; i++) {
      TestForeignKeyBean testForeignKeyBean = new TestForeignKeyBean();
      String fkey = RandomStringUtils.randomAlphabetic(15);
      foreignKeys.add(fkey);
      testForeignKeyBean.setRandomAttribute(fkey);
      testForeignKeyBean.setForeignKey(primaryKey);
      foreignKeyBeans.add(testForeignKeyBean);
    }
    testForeignKeyRepository.save(foreignKeyBeans);

  }

  private void validateResults(List<TestPrimaryKeyBean> actualPrimaryKeyBean) {
    assertNotNull(actualPrimaryKeyBean);
    assertTrue(actualPrimaryKeyBean.size() == 1);
    List<TestForeignKeyBean> actualFKeyBeanList = actualPrimaryKeyBean.get(0).getForeignKeyBeanList();
    assertTrue(actualFKeyBeanList != null && actualFKeyBeanList.size() == foreignKeyItemsCount);
    int fKeyCount = 0;
    Set<TestForeignKeyBean> setOfFKeys = Sets.newHashSet(actualFKeyBeanList);
    // make sure there are no dups in the fkeys
    assertEquals(setOfFKeys.size(), foreignKeyItemsCount);
    actualFKeyBeanList.forEach(fkey -> {
      assertTrue(foreignKeys.contains(fkey.getRandomAttribute()));
    });
  }

  @Test
  public void mustReturnForeignKeyItemsInPrimaryList2() {
    // now read the primary key
    List<TestPrimaryKeyBean> actualPrimaryKeyBeans = testPrimaryKeyRepository.findAllPrimaryKeyBeans2();
    validateResults(actualPrimaryKeyBeans);
  }
}
