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

package com.github.krr.mongodb.aggregate.support.tests.nonreactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import com.github.krr.mongodb.aggregate.support.config.NonReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.nonreactive.CountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 2/20/17.
 */

@SuppressWarnings({"ConstantConditions", "SpringJavaInjectionPointsAutowiringInspection"})
@ContextConfiguration(classes = NonReactiveAggregateTestConfiguration.class)
public class AggregateCountTest extends AbstractTestNGSpringContextTests {

  final String[] COUNT_TEST_DOCS = {"{ \"_id\" : 1, \"subject\" : \"History\", \"score\" : 88 }",
                                    "{ \"_id\" : 2, \"subject\" : \"History\", \"score\" : 92 }",
                                    "{ \"_id\" : 3, \"subject\" : \"History\", \"score\" : 97 }",
                                    "{ \"_id\" : 4, \"subject\" : \"History\", \"score\" : 71 }",
                                    "{ \"_id\" : 5, \"subject\" : \"History\", \"score\" : 79 }",
                                    "{ \"_id\" : 6, \"subject\" : \"History\", \"score\" : 83 }"};
  @Autowired
  private CountRepository countRepository;

  @BeforeClass
  @SuppressWarnings("Duplicates")
  public void setup() {
    countRepository.deleteAll();
    List<Score> scores = getScoresDocuments();
    countRepository.insert(scores);
  }

  List<Score> getScoresDocuments() {
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
    return scores;
  }

  @Test
  public void mustReturnBucketsFromRepository() {
    validateRepository();
    Integer passingScores = countRepository.getPassingScores();
    assertNotNull(passingScores);
    assertEquals((int) passingScores, 4);
  }

  @Test
  public void mustReturnBucketsFromRepository2UsingMetaAnnotation() {
    validateRepository();
    Integer scoresGreaterThan75 = countRepository.scoresGreaterThan75UsingMetaAnnotation();
    assertNotNull(scoresGreaterThan75);
    assertEquals((int) scoresGreaterThan75, 5);
  }

  private void validateRepository() {
    assertNotNull(countRepository, "Must have a repository");
    List<Score> scores = countRepository.findAll();
    assertNotNull(scores);
    assertEquals(scores.size(), COUNT_TEST_DOCS.length);
  }

}
