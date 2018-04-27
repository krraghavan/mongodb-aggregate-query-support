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

package com.github.krr.mongodb.aggregate.support.tests;

import com.github.krr.mongodb.aggregate.support.beans.ScoreResultsBean;
import com.github.krr.mongodb.aggregate.support.beans.TestScoreBean;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.fixtures.AggregateQueryFixtures;
import com.github.krr.mongodb.aggregate.support.repository.TestAddFieldsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by rkolliva
 * 1/24/17.
 */


@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "Duplicates",
                   "SpringJavaInjectionPointsAutowiringInspection"})
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateAddFieldsTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestAddFieldsRepository testAddFieldsRepository;

  @BeforeClass
  private void setupRepository() throws IOException {
    List<TestScoreBean> testScoreBeans = AggregateQueryFixtures.newTestScoresFixture();
    testAddFieldsRepository.save(testScoreBeans);
  }

  @Test
  public void mustAddFieldsToResults() {
    assertNotNull(testAddFieldsRepository);

    List<ScoreResultsBean> resultsBeanList = testAddFieldsRepository.addFieldsToScore();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    validateResults(resultsBeanList);
  }

  @Test
  public void mustAddFieldsToResults2() {
    assertNotNull(testAddFieldsRepository);

    List<ScoreResultsBean> resultsBeanList = testAddFieldsRepository.addFieldsToScore2();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    validateResults(resultsBeanList);
  }

  @SuppressWarnings("Duplicates")
  private void validateResults(List<ScoreResultsBean> resultsBeanList) {
    for (ScoreResultsBean scoreResultsBean : resultsBeanList) {
      int totalHw, totalScore, totalQuiz = 0;
      if(scoreResultsBean.getId() == 1) {
        totalHw = 25;
        totalQuiz = 18;
        totalScore = 43;
      }
      else {
        totalHw = 16;
        totalQuiz = 16;
        totalScore = 40;
      }
      assertEquals(scoreResultsBean.getTotalHomework(), totalHw);
      assertEquals(scoreResultsBean.getTotalQuiz(), totalQuiz);
      assertEquals(scoreResultsBean.getTotalScore(), totalScore);
    }
  }
}
