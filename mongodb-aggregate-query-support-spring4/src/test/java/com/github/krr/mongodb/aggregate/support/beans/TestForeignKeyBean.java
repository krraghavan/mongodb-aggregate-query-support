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

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.annotation.Id;

/**
 * @author rkolliva.
 */
public class TestForeignKeyBean extends AbstractTestAggregateBean {

  @Id
  @BsonProperty("_id")
  private String randomAttribute;

  private String foreignKey;

  public String getRandomAttribute() {
    return randomAttribute;
  }

  public void setRandomAttribute(String randomAttribute) {
    this.randomAttribute = randomAttribute;
  }

  public String getForeignKey() {
    return foreignKey;
  }

  public void setForeignKey(String foreignKey) {
    this.foreignKey = foreignKey;
  }
}
