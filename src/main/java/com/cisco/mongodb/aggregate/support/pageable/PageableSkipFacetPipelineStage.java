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

package com.cisco.mongodb.aggregate.support.pageable;

import com.cisco.mongodb.aggregate.support.annotation.Conditional;
import com.cisco.mongodb.aggregate.support.annotation.v2.FacetPipelineStage;
import com.cisco.mongodb.aggregate.support.annotation.v2.Skip2;

import java.lang.annotation.Annotation;

/**
 * Created by rkolliva
 * 4/7/17.
 */
public class PageableSkipFacetPipelineStage implements FacetPipelineStage {

  private final int offset;

  public PageableSkipFacetPipelineStage(int offset) {
    this.offset = offset;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return FacetPipelineStage.class;
  }

  @Override
  public Class<? extends Annotation> stageType() {
    return Skip2.class;
  }

  @Override
  public String query() {
    return String.valueOf(offset);
  }

  @Override
  public Conditional[] condition() {
    return new Conditional[0];
  }
}
