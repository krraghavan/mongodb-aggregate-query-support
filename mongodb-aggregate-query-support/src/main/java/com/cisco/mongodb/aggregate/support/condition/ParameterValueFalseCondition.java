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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Created by rkolliva
 * 4/15/17.
 */


public class ParameterValueFalseCondition extends AbstractCondition {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterValueNotNullCondition.class);

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    LOGGER.trace(">>>> ParameterValueFalseCondition::matches");
    Object parameter = getParameterByIndex(conditionContext, annotatedTypeMetadata);
    if(Boolean.class.isAssignableFrom(parameter.getClass())) {
      return !(Boolean) parameter;
    }
    int parameterIndex = getParameterIndex(annotatedTypeMetadata);
    throw new IllegalArgumentException("Argument at index " + parameterIndex + " not convertible to boolean");
  }
}
