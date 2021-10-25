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
package com.github.krr.mongodb.aggregate.support.utils;

import com.github.krr.mongodb.aggregate.support.annotations.Conditional;
import com.github.krr.mongodb.aggregate.support.annotations.Conditional.ConditionalMatchType;
import com.github.krr.mongodb.aggregate.support.condition.AggregateQueryMethodConditionContext;
import com.github.krr.mongodb.aggregate.support.condition.ConditionalAnnotationMetadata;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParametersParameterAccessor;
import org.springframework.data.repository.query.Parameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankasa
 */
public class ProcessorUtils {

  public boolean allowStage(Conditional[] conditionalClasses, ConditionalMatchType conditionalMatchType, Method method,
                            MongoParameterAccessor mongoParameterAccessor,
                            ConvertingParameterAccessor convertingParameterAccessor) {
    boolean shouldProcess = true;
    try {
      for (Conditional conditional : conditionalClasses) {
        List<Object> parameterValues = getParameterValues(method, mongoParameterAccessor, convertingParameterAccessor);
        ConditionalAnnotationMetadata metadata = new ConditionalAnnotationMetadata(conditional);
        AggregateQueryMethodConditionContext context = new AggregateQueryMethodConditionContext(method,
                                                                                                parameterValues);
        Object object = conditional.condition().newInstance();
        Assert.isAssignable(Condition.class, object.getClass(), "Class must be a Condition");
        Condition condition = (Condition) object;
        boolean isTrue = condition.matches(context, metadata);
        if (conditionalMatchType == ConditionalMatchType.ANY && isTrue) {
          return true;
        }
        if(conditionalMatchType == ConditionalMatchType.ALL) {
          shouldProcess &= condition.matches(context, metadata);
        }
      }
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException("Could not create an instance of the condition class", e);
    }
    return conditionalMatchType == ConditionalMatchType.ANY ? ArrayUtils.isEmpty(conditionalClasses) : shouldProcess;
  }

  @SuppressWarnings("WeakerAccess")
  public List<Object> getParameterValues(Method method, MongoParameterAccessor mongoParameterAccessor,
                                         ConvertingParameterAccessor convertingParameterAccessor) {
    List<Object> retval = new ArrayList<>();
    int numArgs = method.getParameterCount();
    for (int i = 0; i < numArgs; i++) {
      Parameter param = ((MongoParametersParameterAccessor) mongoParameterAccessor).getParameters().getParameter(i);
      if (param.isBindable()) {
        retval.add(convertingParameterAccessor.getBindableValue(i));
      }
    }
    return retval;
  }
}
