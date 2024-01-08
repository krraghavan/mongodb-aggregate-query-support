/*
 *  Copyright (c) 2018 the original author or authors.
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

import com.github.krr.mongodb.aggregate.support.beans.Score;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.CountRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by rkolliva
 * 1/2/18.
 */

@SuppressWarnings(
    {"SpringJavaAutowiredMembersInspection", "Duplicates"})
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class CollectionNameAnnotationTest extends AggregateCountTest {

  @Autowired
  private CountRepository countRepository;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Test
  public void mustReturnScoresFromDynamicallyCreatedScoresCollection() {
    String collName = RandomStringUtils.randomAlphabetic(10);
    List<Score> scores = getScoresDocuments();
    // insert scores into a random collection
    mongoTemplate.insert(scores, collName);
    validateRepository(collName);
    Integer passingScores = countRepository.getPassingScores2FromSpecifiedCollection(collName);
    assertNotNull(passingScores);
    assertEquals((int) passingScores, 4);

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionIfMultipleCollectionNameAnnotationsPresentOnMethod() {
    String collName = RandomStringUtils.randomAlphabetic(10);
    List<Score> scores = getScoresDocuments();
    // insert scores into a random collection
    mongoTemplate.insert(scores, collName);
    validateRepository(collName);
    Integer passingScores = countRepository.invalidGetPassingScores2FromSpecifiedCollection(collName, new Object(),
                                                                                            collName);
    assertNotNull(passingScores);
    assertEquals((int) passingScores, 4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionIfMultipleCollectionNameAnnotationsPresentOnMethod2() {
    String collName = RandomStringUtils.randomAlphabetic(10);
    List<Score> scores = getScoresDocuments();
    // insert scores into a random collection
    mongoTemplate.insert(scores, collName);
    validateRepository(collName);
    Integer passingScores = countRepository.invalidGetPassingScores2FromSpecifiedCollection2(collName, new Object(),
                                                                                             collName);
    assertNotNull(passingScores);
    assertEquals((int) passingScores, 4);
  }

  private void validateRepository(String collName) {
    assertNotNull(countRepository, "Must have a repository");
    List<Score> scores = mongoTemplate.findAll(Score.class, collName);
    assertNotNull(scores);
    assertEquals(scores.size(), COUNT_TEST_DOCS.length);

  }
}
