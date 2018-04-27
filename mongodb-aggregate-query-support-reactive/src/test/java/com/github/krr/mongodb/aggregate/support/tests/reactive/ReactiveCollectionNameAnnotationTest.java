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

package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.github.krr.mongodb.aggregate.support.beans.Score;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveCountRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 1/2/18.
 */

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveCollectionNameAnnotationTest extends ReactiveAggregateCountTest {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ReactiveCountRepository countRepository;

  @Autowired
  private ReactiveMongoOperations reactiveMongoOperations;

  @Test
  public void mustReturnScoresFromDynamicallyCreatedScoresCollection() {
    String collName = RandomStringUtils.randomAlphabetic(10);
    List<Score> scores = getScoresDocuments();
    // insert scores into a random collection
    reactiveMongoOperations.insert(scores, collName).collectList().block();
    validateRepository(collName);
    Integer passingScores = countRepository.getPassingScores2FromSpecifiedCollection(collName).block();
    assertNotNull(passingScores);
    assertEquals((int) passingScores, 4);

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionIfMultipleCollectionNameAnnotationsPresentOnMethod() {
    String collName = RandomStringUtils.randomAlphabetic(10);
    List<Score> scores = getScoresDocuments();
    // insert scores into a random collection
    reactiveMongoOperations.insert(scores, collName).collectList().block();
    validateRepository(collName);
    Document passingScoresDoc = countRepository.invalidGetPassingScores2FromSpecifiedCollection(collName, new Object(),
                                                                                                collName).block();
    assertNotNull(passingScoresDoc);
    assertTrue(passingScoresDoc.containsKey(PASSING_SCORES));
    assertEquals((int) passingScoresDoc.get(PASSING_SCORES), 4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void mustThrowExceptionIfMultipleCollectionNameAnnotationsPresentOnMethod2() {
    String collName = RandomStringUtils.randomAlphabetic(10);
    List<Score> scores = getScoresDocuments();
    // insert scores into a random collection
    reactiveMongoOperations.insert(scores, collName).collectList().block();
    validateRepository(collName);
    countRepository.invalidGetPassingScores2FromSpecifiedCollection2(collName, new Object(), collName).block();
  }

  private void validateRepository(String collName) {
    assertNotNull(countRepository, "Must have a repository");
    Flux<Score> scores = reactiveMongoOperations.findAll(Score.class, collName);
    assertNotNull(scores);
    assertEquals(scores.collectList().block().size(), COUNT_TEST_DOCS.length);

  }
}
