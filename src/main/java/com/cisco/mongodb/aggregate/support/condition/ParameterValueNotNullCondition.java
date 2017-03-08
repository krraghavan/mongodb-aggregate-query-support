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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * Created by rkolliva
 * 3/7/17.
 */
public class ParameterValueNotNullCondition implements Condition {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterValueNotNullCondition.class);

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    LOGGER.trace(">>>> ParameterValueNotNullCondition::matches");
    Assert.isAssignable(AggregateQueryMethodConditionContext.class, conditionContext.getClass());
    AggregateQueryMethodConditionContext ctx = (AggregateQueryMethodConditionContext)conditionContext;
    List<Object> parameters = ctx.getParameterValues();
    Map<String, Object> params = annotatedTypeMetadata.getAnnotationAttributes(Conditional.class.getName());
    int parameterIndex = (int) params.get(ConditionalAnnotationMetadata.PARAMETER_INDEX);
    LOGGER.trace("<<<< ParameterValueNotNullCondition::matches");
    if(parameterIndex < parameters.size()) {
      return parameters.get(parameterIndex) != null;
    }
    // any invalid parameter will be considered a non-null match.
    return true;
  }
}
