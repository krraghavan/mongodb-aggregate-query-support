package com.cisco.mongodb.aggregate.support.condition;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ParameterValueNullCondition extends ParameterValueNotNullCondition {

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    return !super.matches(conditionContext, annotatedTypeMetadata);
  }

}
