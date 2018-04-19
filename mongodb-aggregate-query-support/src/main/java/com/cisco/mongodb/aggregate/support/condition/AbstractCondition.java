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

package com.cisco.mongodb.aggregate.support.condition;

import com.cisco.mongodb.aggregate.support.annotation.Conditional;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva
 * 4/15/17.
 */


@SuppressWarnings("WeakerAccess")
public abstract class AbstractCondition implements Condition {

  protected Object getParameterByIndex(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    Assert.isAssignable(AggregateQueryMethodConditionContext.class, conditionContext.getClass());
    AggregateQueryMethodConditionContext ctx = (AggregateQueryMethodConditionContext) conditionContext;
    List<Object> parameters = ctx.getParameterValues();
    int parameterIndex = getParameterIndex(annotatedTypeMetadata);
    int paramCount = parameters.size();
    if (parameterIndex < paramCount) {
      return parameters.get(parameterIndex);
    }
    throw new IllegalArgumentException("Argument index " + parameterIndex + " out of bounds, max count: " + paramCount);
  }

  protected int getParameterIndex(AnnotatedTypeMetadata annotatedTypeMetadata) {
    Map<String, Object> params = annotatedTypeMetadata.getAnnotationAttributes(Conditional.class.getName());
    return (int) params.get(ConditionalAnnotationMetadata.PARAMETER_INDEX);
  }
}
