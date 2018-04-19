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

package com.github.krr.mongodb.aggregate.support.test.beans;

import java.util.Objects;

/**
 * Created by rkolliva
 * 1/24/17.
 */


public class ScoreResultsBean extends TestScoreBean {

  private int totalQuiz;

  private int totalScore;

  private int totalHomework;

  public int getTotalQuiz() {
    return totalQuiz;
  }

  public void setTotalQuiz(int totalQuiz) {
    this.totalQuiz = totalQuiz;
  }

  public int getTotalScore() {
    return totalScore;
  }

  public void setTotalScore(int totalScore) {
    this.totalScore = totalScore;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ScoreResultsBean that = (ScoreResultsBean) o;
    return totalQuiz == that.totalQuiz &&
           totalScore == that.totalScore &&
           totalHomework == that.totalHomework;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), totalQuiz, totalScore, totalHomework);
  }

  public int getTotalHomework() {

    return totalHomework;
  }

  public void setTotalHomework(int totalHomework) {
    this.totalHomework = totalHomework;
  }


}
