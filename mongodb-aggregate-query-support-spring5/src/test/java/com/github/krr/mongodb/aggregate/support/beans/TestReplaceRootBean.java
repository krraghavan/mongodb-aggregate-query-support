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

import com.github.krr.mongodb.aggregate.support.annotations.MongoId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva
 * 1/25/17.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TestReplaceRootBean extends AbstractTestAggregateBean {

  @MongoId
  private int id;

  private List<String> fruit;

  private List<String> vegetables;

  @BsonProperty(value = "in_stock")
  private Map<String, Integer> inStock;

  @BsonProperty(value = "on_order")
  private Map<String, Integer> onOrder;

  private String name;

  @BsonProperty(value = "first_name")
  private String firstName;

  @BsonProperty(value = "last_name")
  private String lastName;

  private int age;

  private Map<String, Integer> pets;

  private String city;

  private String aname;

  private List<Map<String, String>> phones;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
