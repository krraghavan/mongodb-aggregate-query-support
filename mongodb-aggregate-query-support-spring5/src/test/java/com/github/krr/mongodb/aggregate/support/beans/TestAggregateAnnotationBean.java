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
 * Created by rkolliva on 10/10/2015.
 */
@Data
public class TestAggregateAnnotationBean extends AbstractTestAggregateBean {

  private String randomAttribute;

  @MongoId
  private String oid;

  public TestAggregateAnnotationBean(){}

  public TestAggregateAnnotationBean(String randomAttribute) {
    this.randomAttribute = randomAttribute;
  }

  public String getRandomAttribute() {
    return randomAttribute;
  }

  public void setRandomAttribute(String randomAttribute) {
    this.randomAttribute = randomAttribute;
  }

  public String getOid() {
    return oid;
  }

  public void setOid(String oid) {
    this.oid = oid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestAggregateAnnotationBean that = (TestAggregateAnnotationBean) o;
    return Objects.equals(randomAttribute, that.randomAttribute) &&
           Objects.equals(oid, that.oid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(randomAttribute, oid);
  }
}
