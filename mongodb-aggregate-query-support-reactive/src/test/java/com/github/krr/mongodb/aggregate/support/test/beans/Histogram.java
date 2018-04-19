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

package com.github.krr.mongodb.aggregate.support.test.beans;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Objects;

/**
 * Created by rkolliva
 * 1/21/17.
 */


public class Histogram {

  @Id
  private String id;

  private List<String> model;

  private int count;

  public Histogram() {
  }

  public Histogram(String id, List<String> model, int count) {
    this.id = id;
    this.model = model;
    this.count = count;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getModel() {
    return model;
  }

  public void setModel(List<String> model) {
    this.model = model;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Histogram histogram = (Histogram) o;
    return count == histogram.count &&
           Objects.equals(id, histogram.id) &&
           Objects.equals(model, histogram.model);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, model, count);
  }
}
