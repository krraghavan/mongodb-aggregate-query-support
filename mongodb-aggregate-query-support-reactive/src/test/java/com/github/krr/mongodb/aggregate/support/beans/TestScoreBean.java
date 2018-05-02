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

package com.github.krr.mongodb.aggregate.support.beans;

import com.github.krr.mongodb.aggregate.support.reactive.annotations.MongoId;

import java.util.List;
import java.util.Objects;

/**
 * Created by rkolliva
 * 1/24/17.
 */
public class TestScoreBean extends AbstractTestAggregateBean {

  @MongoId
  private int id;

  private String student;

  private List<Integer> quiz;

  private int extraCredit;

  private List<Integer> homework;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getStudent() {
    return student;
  }

  public void setStudent(String student) {
    this.student = student;
  }

  public List<Integer> getQuiz() {
    return quiz;
  }

  public void setQuiz(List<Integer> quiz) {
    this.quiz = quiz;
  }

  public int getExtraCredit() {
    return extraCredit;
  }

  public void setExtraCredit(int extraCredit) {
    this.extraCredit = extraCredit;
  }

  public List<Integer> getHomework() {
    return homework;
  }

  public void setHomework(List<Integer> homework) {
    this.homework = homework;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestScoreBean scoreBean = (TestScoreBean) o;
    return id == scoreBean.id &&
           extraCredit == scoreBean.extraCredit &&
           Objects.equals(student, scoreBean.student) &&
           Objects.equals(quiz, scoreBean.quiz) &&
           Objects.equals(homework, scoreBean.homework);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, student, quiz, extraCredit, homework);
  }
}
