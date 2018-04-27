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
package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.github.krr.mongodb.aggregate.support.beans.TestAggregateAnnotation2FieldsBean;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveTestAggregateRepository2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Created by camejavi on 6/9/2016.
 *
 */
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "Duplicates"})
@ContextConfiguration(classes = {ReactiveAggregateTestConfiguration.class})
public class ReactiveAggregateOutTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private ReactiveTestAggregateRepository2 testAggregateRepository2;

  @Autowired
  private ReactiveMongoOperations reactiveMongoOperations;

  @Test
  public void outMustPlaceRepositoryObjectsInDifferentRepository() {
    TestAggregateAnnotation2FieldsBean obj1 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(10));
    obj1.setOid(UUID.randomUUID().toString());
    TestAggregateAnnotation2FieldsBean obj2 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(20),
                                                                                     nextInt(1, 10000));
    obj2.setOid(UUID.randomUUID().toString());

    testAggregateRepository2.save(obj1).block();
    testAggregateRepository2.save(obj2).block();
    String outputRepoName = "temp1";
    testAggregateRepository2.aggregateQueryWithOut(outputRepoName).block();
    assertTrue(reactiveMongoOperations.collectionExists(outputRepoName).block());
    Flux<TestAggregateAnnotation2FieldsBean> copiedObjsFlux = reactiveMongoOperations
        .findAll(TestAggregateAnnotation2FieldsBean.class,
                 outputRepoName);
    List<TestAggregateAnnotation2FieldsBean> copiedObjs = copiedObjsFlux.collectList().block();
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

  @Test
  public void outMustPlaceRepositoryObjectsInDifferentRepositoryIfOtherQueryAnnotationsArePresent() {
    String randomStr = randomAlphabetic(10);
    TestAggregateAnnotation2FieldsBean obj1 = new TestAggregateAnnotation2FieldsBean(randomStr);
    TestAggregateAnnotation2FieldsBean obj2 = new TestAggregateAnnotation2FieldsBean(randomAlphabetic(20),
                                                                                     nextInt(1, 10000));
    TestAggregateAnnotation2FieldsBean obj3 = new TestAggregateAnnotation2FieldsBean(randomStr, nextInt(1, 10000));
    testAggregateRepository2.save(obj1).block();
    testAggregateRepository2.save(obj2).block();
    testAggregateRepository2.save(obj3).block();
    String outputRepoName = "tempBroken";
    testAggregateRepository2.aggregateQueryWithMatchAndOut(randomStr, outputRepoName).block();
    assertTrue(reactiveMongoOperations.collectionExists(outputRepoName).block());
    List<TestAggregateAnnotation2FieldsBean> copiedObjs = reactiveMongoOperations.findAll(TestAggregateAnnotation2FieldsBean.class,
                                                                                          outputRepoName)
                                                                                .collectList().block();
    //clear testAggregateAnnotationFieldsBean repo before running this test
    assertSame(copiedObjs.size(), 2);
  }
}
