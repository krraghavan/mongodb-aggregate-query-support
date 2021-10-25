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

import java.util.List;

/**
 * Created by rkolliva
 * 4/6/17.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArtworkBucketTestBean  extends AbstractTestAggregateBean {

  @MongoId
  private Integer id;

  private String title;

  private String artist;

  private int year;

  private double price;

  private List<String> tags;
}
