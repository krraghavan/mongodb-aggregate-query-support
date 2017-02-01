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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;

import java.util.Map;

/**
 * Created by rkolliva
 * 1/25/17.
 */


public class TestReplaceRootBean extends AbstractTestAggregateBean {

  @Id
  private int id;

  private String fruit;

  private Map<String, Integer> in_stock;

  private Map<String, Integer> on_order;

  private String name;

  private String first_name;

  private String last_name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFruit() {
    return fruit;
  }

  public void setFruit(String fruit) {
    this.fruit = fruit;
  }

  public Map<String, Integer> getIn_stock() {
    return in_stock;
  }

  public void setIn_stock(Map<String, Integer> in_stock) {
    this.in_stock = in_stock;
  }

  public Map<String, Integer> getOn_order() {
    return on_order;
  }

  public void setOn_order(Map<String, Integer> on_order) {
    this.on_order = on_order;
  }

  public String getFirst_name() {
    return first_name;
  }

  public void setFirst_name(String first_name) {
    this.first_name = first_name;
  }

  public String getLast_name() {
    return last_name;
  }

  public void setLast_name(String last_name) {
    this.last_name = last_name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
