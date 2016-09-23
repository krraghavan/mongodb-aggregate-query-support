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
package com.cisco.mongodb.aggregate.support.test.beans;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkolliva on 10/19/2015.
 */
public class TestUnwindAggregateAnnotationBean extends AbstractTestAggregateBean {

  @Id
  private String oid;

  private List<String> randomListOfStrings = new ArrayList<>();

  private String randomSingleField;

  private int randomIntField;

  public String getOid() {
    return oid;
  }

  public void setOid(String oid) {
    this.oid = oid;
  }

  public List<String> getRandomListOfStrings() {
    return randomListOfStrings;
  }

  public void setRandomListOfStrings(List<String> randomListOfStrings) {
    this.randomListOfStrings = randomListOfStrings;
  }

  public String getRandomSingleField() {
    return randomSingleField;
  }

  public void setRandomSingleField(String randomSingleField) {
    this.randomSingleField = randomSingleField;
  }

  public int getRandomIntField() {
    return randomIntField;
  }

  public void setRandomIntField(int randomIntField) {
    this.randomIntField = randomIntField;
  }
}
