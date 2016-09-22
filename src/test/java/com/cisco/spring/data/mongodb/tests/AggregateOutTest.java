// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.tests;

import com.cisco.spring.data.mongodb.repository.test.aggregate.TestAggregateRepository2;
import com.cisco.spring.data.mongodb.test.beans.TestAggregateAnnotation2FieldsBean;
import com.cisco.spring.data.mongodb.test.config.aggregate.AggregateTestConfiguration;
import com.mongodb.DBCursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Created by camejavi on 6/9/2016.
 */
@ContextConfiguration(classes = {AggregateTestConfiguration.class})
public class AggregateOutTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestAggregateRepository2 testAggregateRepository2;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Test(enabled = false)
  public void outMustPlaceRepositoryObjectsInDifferentRepository() {
    TestAggregateAnnotation2FieldsBean obj1 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(10));
    TestAggregateAnnotation2FieldsBean obj2 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(20),
                                                                                     nextInt(1, 10000));
    testAggregateRepository2.save(obj1);
    testAggregateRepository2.save(obj2);
    String outputRepoName = "temp1";
    testAggregateRepository2.aggregateQueryWithOut(outputRepoName);
    assertTrue(mongoTemplate.collectionExists(outputRepoName));
    List<TestAggregateAnnotation2FieldsBean> copiedObjs = mongoTemplate.findAll(TestAggregateAnnotation2FieldsBean.class,
                                                                                outputRepoName);
    //clear testAggregateAnnotationFieldsBean repo before running this test
    assertSame(copiedObjs.size(), 2);
    if (copiedObjs.get(0).getRandomAttribute2() == 0) {
      assertTrue(copiedObjs.get(0).equals(obj1));
      assertTrue(copiedObjs.get(1).equals(obj2));
    }
    else {
      assertSame(copiedObjs.get(0), obj2);
      assertSame(copiedObjs.get(1), obj1);
    }
  }

  @Test(enabled = false)
  public void outMustPlaceRepositoryObjectsInDifferentRepositoryIfOtherQueryAnnotationsArePresent() {
    String randomStr = randomAlphabetic(10);
    TestAggregateAnnotation2FieldsBean obj1 = new TestAggregateAnnotation2FieldsBean(randomStr);
    TestAggregateAnnotation2FieldsBean obj2 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(20),
                                                                                     nextInt(1, 10000));
    TestAggregateAnnotation2FieldsBean obj3 = new TestAggregateAnnotation2FieldsBean(randomStr, nextInt(1, 10000));
    testAggregateRepository2.save(obj1);
    testAggregateRepository2.save(obj2);
    testAggregateRepository2.save(obj3);
    String outputRepoName = "tempBroken";
    testAggregateRepository2.aggregateQueryWithMatchAndOut(randomStr, outputRepoName);
    assertTrue(mongoTemplate.collectionExists(outputRepoName));
    List<TestAggregateAnnotation2FieldsBean> copiedObjs = mongoTemplate.findAll(TestAggregateAnnotation2FieldsBean.class,
                                                                                outputRepoName);
    //clear testAggregateAnnotationFieldsBean repo before running this test
    assertSame(copiedObjs.size(), 2);
    DBCursor dbCursor = mongoTemplate.getCollection("tempBroken").find();
    assertTrue(dbCursor.hasNext());
  }
}
