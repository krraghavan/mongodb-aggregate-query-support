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

import org.jongo.marshall.jackson.oid.MongoId;
import org.springframework.data.annotation.Id;

/**
 * Created by rkolliva
 * 5/13/17.
 */
public class TestLongBean {

  private Long randomLong;

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
}
