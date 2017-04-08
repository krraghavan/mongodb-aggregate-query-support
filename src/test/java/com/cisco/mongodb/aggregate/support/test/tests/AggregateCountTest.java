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

import com.cisco.mongodb.aggregate.support.test.beans.Score;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.CountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by rkolliva
 * 2/20/17.
 */

@SuppressWarnings("ConstantConditions")
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateCountTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private CountRepository countRepository;

  private final String[] COUNT_TEST_DOCS = {"{ \"id\" : 1, \"subject\" : \"History\", \"score\" : 88 }",
                                            "{ \"id\" : 2, \"subject\" : \"History\", \"score\" : 92 }",
                                            "{ \"id\" : 3, \"subject\" : \"History\", \"score\" : 97 }",
                                            "{ \"id\" : 4, \"subject\" : \"History\", \"score\" : 71 }",
                                            "{ \"id\" : 5, \"subject\" : \"History\", \"score\" : 79 }",
                                            "{ \"id\" : 6, \"subject\" : \"History\", \"score\" : 83 }"};


  @BeforeClass
  @SuppressWarnings("Duplicates")
  public void setup() throws Exception {
    countRepository.deleteAll();
    ObjectMapper mapper = new ObjectMapper();
    List<Score> scores = new ArrayList<>();
    Arrays.asList(COUNT_TEST_DOCS).forEach((s) -> {
      try {
        scores.add(mapper.readValue(s, Score.class));
      }
      catch (IOException e) {
        assertTrue(false, e.getMessage());
      }
    });
    countRepository.insert(scores);
  }

  @Test
  public void mustReturnBucketsFromRepository() {
    assertNotNull(countRepository, "Must have a repository");
    List<Score> scores = countRepository.findAll();
    assertNotNull(scores);
    assertEquals(scores.size(), COUNT_TEST_DOCS.length);
    Integer passingScores = countRepository.getPassingScores();
    assertNotNull(passingScores);
    assertEquals((int)passingScores, 4);
  }

  @Test
  public void mustReturnBucketsFromRepository2() {
    assertNotNull(countRepository, "Must have a repository");
    List<Score> scores = countRepository.findAll();
    assertNotNull(scores);
    assertEquals(scores.size(), COUNT_TEST_DOCS.length);
    Integer passingScores = countRepository.getPassingScores2();
    assertNotNull(passingScores);
    assertEquals((int)passingScores, 4);
  }



}
