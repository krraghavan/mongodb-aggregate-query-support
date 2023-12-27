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



package com.github.krr.mongodb.aggregate.support.beans;

import com.github.krr.mongodb.aggregate.support.nonreactive.annotations.MongoId;
import org.springframework.data.annotation.Transient;

import java.util.List;

/**
 * @author rkolliva.
 */
public class TestPrimaryKeyBean extends AbstractTestAggregateBean {

  @MongoId
  private String randomPrimaryKey;

  @Transient
  private List<TestForeignKeyBean> foreignKeyBeanList;

  public String getRandomPrimaryKey() {
    return randomPrimaryKey;
  }

  public void setRandomPrimaryKey(String randomPrimaryKey) {
    this.randomPrimaryKey = randomPrimaryKey;
  }

  public List<TestForeignKeyBean> getForeignKeyBeanList() {
    return foreignKeyBeanList;
  }

  public void setForeignKeyBeanList(List<TestForeignKeyBean> foreignKeyBeanList) {
    this.foreignKeyBeanList = foreignKeyBeanList;
  }
}
