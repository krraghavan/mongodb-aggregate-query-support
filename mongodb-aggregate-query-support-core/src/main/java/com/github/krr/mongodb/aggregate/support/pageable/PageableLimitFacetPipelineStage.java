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

package com.github.krr.mongodb.aggregate.support.pageable;

import com.github.krr.mongodb.aggregate.support.annotations.Conditional;
import com.github.krr.mongodb.aggregate.support.annotations.FacetPipelineStage;
import com.github.krr.mongodb.aggregate.support.annotations.Limit;

import java.lang.annotation.Annotation;

/**
 * Created by rkolliva
 * 4/7/17.
 */
public class PageableLimitFacetPipelineStage implements FacetPipelineStage {

  private final long pageSize;

  public PageableLimitFacetPipelineStage(long pageSize) {
    this.pageSize = pageSize;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return FacetPipelineStage.class;
  }

  @Override
  public Class<? extends Annotation> stageType() {
    return Limit.class;
  }

  @Override
  public String query() {
    return String.valueOf(pageSize);
  }

  @Override
  public Conditional[] condition() {
    return new Conditional[0];
  }

  @Override
  public Conditional.ConditionalMatchType conditionMatchType() {
    return Conditional.ConditionalMatchType.ANY;
  }
}
