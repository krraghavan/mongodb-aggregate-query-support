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

import com.cisco.mongodb.aggregate.support.test.beans.ArtworkBucketTestBean;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.fixtures.AggregateQueryFixtures;
import com.cisco.mongodb.aggregate.support.test.repository.TestBucketRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by rkolliva
 * 1/21/17.
 */

@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateBucketTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestBucketRepository bucketRepository;

  private String expectedResults = "[{\n" +
                                   "  \"_id\" : 0,\n" +
                                   "  \"count\" : 4,\n" +
                                   "  \"titles\" : [\n" +
                                   "    \"The Pillars of Society\",\n" +
                                   "    \"Dancer\",\n" +
                                   "    \"The Great Wave off Kanagawa\",\n" +
                                   "    \"Blue Flower\"\n" +
                                   "  ]\n" +
                                   "},\n" +
                                   "{\n" +
                                   "  \"_id\" : 200,\n" +
                                   "  \"count\" : 2,\n" +
                                   "  \"titles\" : [\n" +
                                   "    \"Melancholy III\",\n" +
                                   "    \"Composition VII\"\n" +
                                   "  ]\n" +
                                   "},\n" +
                                   "{\n" +
                                   "  \"_id\" : \"Other\",\n" +
                                   "  \"count\" : 2,\n" +
                                   "  \"titles\" : [\n" +
                                   "    \"The Persistence of Memory\",\n" +
                                   "    \"The Scream\"\n" +
                                   "  ]\n" +
                                   "}]";
  @BeforeClass
  private void setupRepository() throws IOException {
    List<ArtworkBucketTestBean> artworks = AggregateQueryFixtures.newArtworkBucketBeans();
    bucketRepository.save(artworks);
  }

  @Test
  public void mustReturnBucketsFromRepository() throws IOException {
    assertNotNull(bucketRepository, "Must have a repository");
    List<Map<String, Object>> actualResults = bucketRepository.getBucketResults();
    assertNotNull(actualResults);
    assertEquals(actualResults.size(), 3);

    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<Map<String, Object>>> expectedResultType = new TypeReference<List<Map<String, Object>>>() {};
    List<Map<String, Object>> expectedResultList = mapper.readValue(expectedResults, expectedResultType);

    validateResult(actualResults, expectedResultList);
  }

  @Test
  public void mustReturnBucketsFromRepository2() throws IOException {
    assertNotNull(bucketRepository, "Must have a repository");
    List<Map<String, Object>> actualResults = bucketRepository.getBucketResults2();
    assertNotNull(actualResults);
    assertEquals(actualResults.size(), 3);
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<Map<String, Object>>> expectedResultType = new TypeReference<List<Map<String, Object>>>() {
    };
    List<Map<String, Object>> expectedResultList = mapper.readValue(expectedResults, expectedResultType);

    validateResult(actualResults, expectedResultList);
  }

  private void validateResult(List<Map<String, Object>> actualResults, List<Map<String, Object>> expectedResultList) {
    assertEquals(expectedResultList.size(), 3);
    for (int i = 0; i < expectedResultList.size(); i++) {
      assertEquals(actualResults.get(0).get("_id"), (Integer) expectedResultList.get(0).get("_id"));
      assertEquals(actualResults.get(0).get("count"), expectedResultList.get(0).get("count"));
      List<String> titles = (List<String>) actualResults.get(0).get("titles");
      List<String> expectedTitles = (List<String>) expectedResultList.get(0).get("titles");
      assertEquals(titles.size(), ((List<String>) expectedResultList.get(0).get("titles")).size());
      for (int j = 0; j < titles.size(); j++) {
        assertEquals(titles.get(j), expectedTitles.get(j));
      }
    }
  }

}
