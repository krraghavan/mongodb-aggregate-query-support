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

import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import org.jongo.marshall.jackson.oid.MongoId;
import org.springframework.data.annotation.Id;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

/**
 * Created by rkolliva
 * 3/1/17.
 */

@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class Possessions {

  @Id
  @MongoId
  private String id;

  private List<Asset> assets;

  private Long sortTestNumber;

  private String tag;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Asset> getAssets() {
    return assets;
  }

  public void setAssets(List<Asset> assets) {
    this.assets = assets;
  }

  public Long getSortTestNumber() {
    return sortTestNumber;
  }

  public void setSortTestNumber(Long sortTestNumber) {
    this.sortTestNumber = sortTestNumber;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }
}
