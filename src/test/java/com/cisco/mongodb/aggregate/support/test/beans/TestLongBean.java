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

package com.cisco.mongodb.aggregate.support.test.beans;

import com.google.common.base.Objects;
import org.jongo.marshall.jackson.oid.MongoId;
import org.springframework.data.annotation.Id;

/**
 * Created by rkolliva
 * 5/13/17.
 */
public class TestLongBean {

  private Long randomLong;

  private String randomString;

  private Long randomLong2;

  @Id @MongoId
  private String id;

  public Long getRandomLong() {
    return randomLong;
  }

  public void setRandomLong(Long randomLong) {
    this.randomLong = randomLong;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRandomString() {
    return randomString;
  }

  public void setRandomString(String randomString) {
    this.randomString = randomString;
  }

  public Long getRandomLong2() {
    return randomLong2;
  }

  public void setRandomLong2(Long randomLong2) {
    this.randomLong2 = randomLong2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestLongBean longBean = (TestLongBean) o;
    return Objects.equal(randomLong, longBean.randomLong) &&
           Objects.equal(randomString, longBean.randomString) &&
           Objects.equal(randomLong2, longBean.randomLong2) &&
           Objects.equal(id, longBean.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(randomLong, randomString, randomLong2, id);
  }
}
