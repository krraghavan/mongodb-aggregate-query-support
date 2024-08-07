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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.beans.*;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveTestAggregateRepository;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveTestAggregateRepository2;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveTestAggregateRepositoryForUnwind;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveTestGroupRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 10/10/2015.
 */
@SuppressWarnings({"Duplicates"})
@ContextConfiguration(classes = {ReactiveAggregateTestConfiguration.class})
public class ReactiveAggregationAnnotationTests extends AbstractTestNGSpringContextTests {

  @Autowired
  private ReactiveTestAggregateRepository testAggregateRepository;

  @Autowired
  private ReactiveTestAggregateRepository2 testAggregateRepository2;

  @Autowired
  private ReactiveTestAggregateRepositoryForUnwind testAggregateRepositoryForUnwind;

  @Autowired
  private ReactiveTestGroupRepository testGroupRepository;

  private List<TestGroupDataBean> groupDataBeans = new ArrayList<>();

  @Test
  public void mustCreateMatchAggregationOperation() {
    assertNotNull(testAggregateRepository);
    String value = randomAlphabetic(10);
    String oid = UUID.randomUUID().toString();
    TestAggregateAnnotationBean bean = new TestAggregateAnnotationBean(value);
    bean.setOid(oid);
    testAggregateRepository.save(bean).block();
    TestAggregateAnnotationBean result = testAggregateRepository.aggregateQueryMatchAnnotation(value).block();
    assertNotNull(result);
    assertEquals(result, bean);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void mustReturnCorrectValuesForMatchWithProjectionOperation() {
    assertNotNull(testAggregateRepository2);
    String value = randomAlphabetic(10);
    String oid = UUID.randomUUID().toString();
    TestAggregateAnnotation2FieldsBean bean = new TestAggregateAnnotation2FieldsBean(value);
    bean.setOid(oid);
    int intValue1 = nextInt(10000, 100000);
    bean.setRandomAttribute2(intValue1);
    testAggregateRepository2.save(bean).block();
    String oid2 = UUID.randomUUID().toString();
    String value2 = randomAlphabetic(10);
    TestAggregateAnnotation2FieldsBean bean2 = new TestAggregateAnnotation2FieldsBean(value2);
    bean.setOid(oid2);
    testAggregateRepository2.save(bean2).block();
    Object result = testAggregateRepository2.aggregateQueryWithMatchAndProjection(intValue1);
    assertNotNull(result);
    assertTrue(Flux.class.isAssignableFrom(result.getClass()));
    List resultList = (List) ((Flux) result).collectList().block();
    assertNotNull(resultList);
    assertEquals(resultList.size(), 1);
    assertTrue(Map.class.isAssignableFrom(resultList.get(0).getClass()));
    Map<String, String> values = (Map<String, String>) resultList.get(0);
    assertEquals(value, values.get("randomAttribute"));
  }

  @Test
  public void mustReturnCorrectValuesForMatchWithLimit() {
    assertNotNull(testAggregateRepository2);

    //create bean 0
    String value0 = randomAlphabetic(10);
    String oid0 = UUID.randomUUID().toString();
    TestAggregateAnnotation2FieldsBean bean0 = new TestAggregateAnnotation2FieldsBean(value0);
    bean0.setOid(oid0);
    int intValue0 = nextInt(10000, 100000);
    bean0.setRandomAttribute2(intValue0);
    testAggregateRepository2.save(bean0).block();

    //create bean 1
    String value1 = randomAlphabetic(10);
    String oid1 = UUID.randomUUID().toString();
    TestAggregateAnnotation2FieldsBean bean1 = new TestAggregateAnnotation2FieldsBean(value1);
    bean1.setOid(oid1);
    int intValue = nextInt(10000, 100000);
    bean1.setRandomAttribute2(intValue);
    testAggregateRepository2.save(bean1).block();

    //create bean 2
    String oid2 = UUID.randomUUID().toString();
    String value2 = randomAlphabetic(10);
    TestAggregateAnnotation2FieldsBean bean2 = new TestAggregateAnnotation2FieldsBean(value2);
    bean2.setOid(oid2);
    bean2.setRandomAttribute2(intValue); //set same intValue
    testAggregateRepository2.save(bean2).block();

    //create bean 2
    String oid3 = UUID.randomUUID().toString();
    TestAggregateAnnotation2FieldsBean bean3 = new TestAggregateAnnotation2FieldsBean(value2);
    bean2.setOid(oid3);
    bean2.setRandomAttribute2(intValue); //set same intValue
    testAggregateRepository2.save(bean3).block();

    //query by limit and attribute2
    Object result = testAggregateRepository2.aggregateQueryWithMatchAndLimit(intValue, 2);
    assertNotNull(result);
    assertTrue(Flux.class.isAssignableFrom(result.getClass()));
    List resultList = (List) ((Flux) result).collectList().block();
    assertNotNull(resultList);
    assertEquals(resultList.size(), 2);
  }

  @Test
  public void mustProcessUnwindAnnotationCorrectly() {
    assertNotNull(testAggregateRepositoryForUnwind);
    testAggregateRepositoryForUnwind.deleteAll().block();
    String oid = UUID.randomUUID().toString();
    TestUnwindAggregateAnnotationBean bean = new TestUnwindAggregateAnnotationBean();
    bean.setOid(oid);
    int intValue1 = nextInt(100000, 200000);
    bean.setRandomIntField(intValue1);
    // generate a list of up to 20 random strings.
    int randomListCount = nextInt(1, 20);
    List<String> randomStringList = new ArrayList<>(randomListCount);
    for (int i = 0; i < randomListCount; i++) {
      randomStringList.add(randomAlphabetic(10));
    }
    bean.setRandomListOfStrings(randomStringList);
    testAggregateRepositoryForUnwind.save(bean).block();
    List<Document> result = testAggregateRepositoryForUnwind.aggregateQueryWithOnlyUnwind(intValue1).collectList().block();
    assertNotNull(result);
    assertTrue(List.class.isAssignableFrom(result.getClass()));
    assertEquals(randomListCount, result.size());
    for (int i = 0; i < randomListCount; i++) {
      Map actualBean = result.get(i);
      assertTrue(actualBean.containsKey("randomListOfStrings"));
      assertTrue(actualBean.get("randomListOfStrings") instanceof String);
      String str = (String) actualBean.get("randomListOfStrings");
      assertTrue(randomStringList.contains(str));
    }
  }

  @Test
  public void mustProcessGroupAnnotationCorrectly() throws IOException {
    populateGroupCollection();
    List<TestGroupResultsBean> results = testGroupRepository.salesBySalesDate().collectList().block();
    assertNotNull(results);

    Document totalQuantity = testGroupRepository.getTotalQuantityForOneItem("abc").block();
    assertNotNull(totalQuantity);
    assertEquals(12, totalQuantity.get("totalQuantity"));

    totalQuantity = testGroupRepository.getTotalQuantityForOneItem("xyz").block();
    assertNotNull(totalQuantity);
    assertEquals(30, totalQuantity.get("totalQuantity"));

    totalQuantity = testGroupRepository.getTotalQuantityForOneItem("jkl").block();
    assertNotNull(totalQuantity);
    assertEquals(1, totalQuantity.get("totalQuantity"));

    Document noQuantity = testGroupRepository.getTotalQuantityForOneItem(RandomStringUtils.random(10)).block();
    assertNull(noQuantity);
  }

  private void populateGroupCollection() throws IOException {

    // Example for group by taken from MongoDB $group documentation https://docs.mongodb.org/manual/reference/operator/aggregation/group/
    String item1 = "{\"_id\" : 1, \"item\" : \"abc\", \"price\" : 10, \"quantity\" : 2, \"date\" : \"2014-03-01T08:00:00Z\"}";
    String item2 = "{ \"_id\" : 2, \"item\" : \"jkl\", \"price\" : 20, \"quantity\" : 1, \"date\" : \"2014-03-01T09:00:00Z\"}";
    String item3 = "{ \"_id\" : 3, \"item\" : \"xyz\", \"price\" : 5, \"quantity\" : 10, \"date\" : \"2014-03-15T09:00:00Z\"}";
    String item4 = "{ \"_id\" : 4, \"item\" : \"xyz\", \"price\" : 5, \"quantity\" : 20, \"date\" : \"2014-04-04T11:21:39.736Z\"}";
    String item5 = "{ \"_id\" : 5, \"item\" : \"abc\", \"price\" : 10, \"quantity\" : 10, \"date\" : \"2014-04-04T21:23:13.331Z\"}";

    ObjectMapper objectMapper = new ObjectMapper();
    groupDataBeans.add(objectMapper.readValue(item1, TestGroupDataBean.class));
    groupDataBeans.add(objectMapper.readValue(item2, TestGroupDataBean.class));
    groupDataBeans.add(objectMapper.readValue(item3, TestGroupDataBean.class));
    groupDataBeans.add(objectMapper.readValue(item4, TestGroupDataBean.class));
    groupDataBeans.add(objectMapper.readValue(item5, TestGroupDataBean.class));
    testGroupRepository.saveAll(groupDataBeans).blockLast();
  }
}
