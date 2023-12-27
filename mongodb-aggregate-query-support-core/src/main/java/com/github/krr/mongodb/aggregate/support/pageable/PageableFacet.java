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
import com.github.krr.mongodb.aggregate.support.annotations.Facet;
import com.github.krr.mongodb.aggregate.support.annotations.FacetPipeline;

import java.lang.annotation.Annotation;


/**
 * Created by rkolliva
 * 4/6/17.
 */
public class PageableFacet implements Facet {

  private final int order;

  private final long offset;

  private final long pageSize;

  public PageableFacet(int order, long offset, long pageSize) {
    this.order = order;
    this.offset = offset;
    this.pageSize = pageSize;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Facet.class;
  }

  @Override
  public FacetPipeline[] pipelines() {
    return new FacetPipeline[] {
        new ResultFacet(offset, pageSize),
        new ResultSetCountFacet()
    };
  }

  @Override
  public int order() {
    return order;
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
