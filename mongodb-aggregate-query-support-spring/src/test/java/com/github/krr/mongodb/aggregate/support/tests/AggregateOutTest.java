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

import com.github.krr.mongodb.aggregate.support.beans.TestAggregateAnnotation2FieldsBean;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.TestAggregateRepository2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.testng.Assert.*;

/**
 * Created by camejavi on 6/9/2016.
 *
 */
@ContextConfiguration(classes = {AggregateTestConfiguration.class})
public class AggregateOutTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestAggregateRepository2 testAggregateRepository2;

  @Autowired
  private MongoTemplate mongoTemplate;

  @DataProvider
  private Object[][] outRepoFixtures() {
    return new Object[][] {
        new Object[]{true},
        new Object[]{false},
        };
  }

  @Test(dataProvider = "outRepoFixtures")
  public void outMustPlaceRepositoryObjectsInDifferentRepository(boolean quotesInPlaceholder) {
    TestAggregateAnnotation2FieldsBean obj1 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(10));
    obj1.setOid(UUID.randomUUID().toString());
    TestAggregateAnnotation2FieldsBean obj2 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(20),
                                                                                     nextInt(1, 10000));
    obj2.setOid(UUID.randomUUID().toString());
    String srcRepo = RandomStringUtils.randomAlphabetic(12);

    mongoTemplate.save(obj1, srcRepo);
    mongoTemplate.save(obj2, srcRepo);
    String outputRepoName = RandomStringUtils.randomAlphabetic(10);
    if(quotesInPlaceholder) {
      testAggregateRepository2.aggregateQueryWithOut(outputRepoName, srcRepo);
    }
    else {
      testAggregateRepository2.aggregateQueryWithOutNoQuotes(outputRepoName, srcRepo);
    }
    assertTrue(mongoTemplate.collectionExists(outputRepoName));
    List<TestAggregateAnnotation2FieldsBean> copiedObjs = mongoTemplate.findAll(TestAggregateAnnotation2FieldsBean.class,
                                                                                outputRepoName);
    //clear testAggregateAnnotationFieldsBean repo before running this test
    assertSame(copiedObjs.size(), 2);
    if (copiedObjs.get(0).getRandomAttribute2() == 0) {
      assertEquals(obj1, copiedObjs.get(0));
      assertEquals(obj2, copiedObjs.get(1));
    }
    else {
      assertSame(copiedObjs.get(0), obj2);
      assertSame(copiedObjs.get(1), obj1);
    }
  }

  @Test(dataProvider = "outRepoFixtures")
  public void outMustPlaceRepositoryObjectsInDifferentRepositoryIfOtherQueryAnnotationsArePresent(boolean quotesInPh) {
    String randomStr = randomAlphabetic(10);
    TestAggregateAnnotation2FieldsBean obj1 = new TestAggregateAnnotation2FieldsBean(randomStr);
    TestAggregateAnnotation2FieldsBean obj2 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(20),
                                                                                     nextInt(1, 10000));
    TestAggregateAnnotation2FieldsBean obj3 = new TestAggregateAnnotation2FieldsBean(randomStr, nextInt(1, 10000));

    String srcRepo = RandomStringUtils.randomAlphabetic(12);
    mongoTemplate.save(obj1, srcRepo);
    mongoTemplate.save(obj2, srcRepo);
    mongoTemplate.save(obj3, srcRepo);
    String outputRepoName = RandomStringUtils.randomAlphabetic(10);
    if(quotesInPh) {
      testAggregateRepository2.aggregateQueryWithMatchAndOut(randomStr, outputRepoName, srcRepo);
    }
    else {
      testAggregateRepository2.aggregateQueryWithMatchAndOutNoQuotes(randomStr, outputRepoName, srcRepo);
    }
    assertTrue(mongoTemplate.collectionExists(outputRepoName));
    List<TestAggregateAnnotation2FieldsBean> copiedObjs = mongoTemplate.findAll(TestAggregateAnnotation2FieldsBean.class,
                                                                                outputRepoName);
    // only two beans match random string out of the 3 inserted.
    assertSame(copiedObjs.size(), 2);
  }
}
