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

import com.github.krr.mongodb.aggregate.support.annotation.Conditional;
import com.github.krr.mongodb.aggregate.support.annotation.FacetPipeline;
import com.github.krr.mongodb.aggregate.support.annotation.FacetPipelineStage;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

/**
 * Created by rkolliva
 * 4/7/17.
 */
public class ResultSetCountFacet implements FacetPipeline {

  @Override
  public Class<? extends Annotation> annotationType() {
    return FacetPipeline.class;
  }

  @Override
  public String name() {
    return "resultSetCountFacet";
  }

  @Override
  public FacetPipelineStage[] stages() {
    return new FacetPipelineStage[] {new PageableTotalCountFacetPipelineStage()};
  }

  @Override
  public String query() {
    return StringUtils.EMPTY;
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
