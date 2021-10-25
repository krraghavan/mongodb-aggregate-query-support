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


import com.github.krr.mongodb.aggregate.support.annotations.MongoId;
import lombok.Data;

import java.util.Objects;

/**
 * Created by rkolliva on 10/18/2015.
 */
@Data
public class TestAggregateAnnotation2FieldsBean extends AbstractTestAggregateBean {

  private String randomAttribute1;

  private int randomAttribute2;

  @MongoId
  private String oid;

  public TestAggregateAnnotation2FieldsBean(String randomAttribute1, int randomAttribute2) {
    this.randomAttribute1 = randomAttribute1;
    this.randomAttribute2 = randomAttribute2;
  }

  public TestAggregateAnnotation2FieldsBean() {
  }

  public TestAggregateAnnotation2FieldsBean(String randomAttribute1) {
    this.randomAttribute1 = randomAttribute1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestAggregateAnnotation2FieldsBean that = (TestAggregateAnnotation2FieldsBean) o;
    return Objects.equals(randomAttribute2, that.randomAttribute2) &&
           Objects.equals(randomAttribute1, that.randomAttribute1) &&
           Objects.equals(oid, that.oid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(randomAttribute1, randomAttribute2, oid);
  }
}
