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

import com.github.krr.mongodb.aggregate.support.annotation.Conditional;
import com.github.krr.mongodb.aggregate.support.condition.AggregateQueryMethodConditionContext;
import com.github.krr.mongodb.aggregate.support.condition.ConditionalAnnotationMetadata;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorUtils.class);

  public boolean allowStage(Conditional[] conditionalClasses, Conditional.ConditionalMatchType conditionalMatchType, Method method,
                            MongoParameterAccessor mongoParameterAccessor,
                            ConvertingParameterAccessor convertingParameterAccessor) {
    boolean shouldProcess = true;
    try {
      for (Conditional conditional : conditionalClasses) {
        List<Object> parameterValues = getParameterValues(method, mongoParameterAccessor, convertingParameterAccessor);
        ConditionalAnnotationMetadata metadata = new ConditionalAnnotationMetadata(conditional);
        AggregateQueryMethodConditionContext context = new AggregateQueryMethodConditionContext(method,
                                                                                                parameterValues);
        Condition condition = conditional.condition().newInstance();
        boolean isTrue = condition.matches(context, metadata);
        if (conditionalMatchType == Conditional.ConditionalMatchType.ANY && isTrue) {
          return true;
        }
        if(conditionalMatchType == Conditional.ConditionalMatchType.ALL) {
          shouldProcess &= condition.matches(context, metadata);
        }
      }
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException("Could not create an instance of the condition class", e);
    }
    return conditionalMatchType == Conditional.ConditionalMatchType.ANY ? ArrayUtils.isEmpty(conditionalClasses) : shouldProcess;
  }

  private List<Object> getParameterValues(Method method, MongoParameterAccessor mongoParameterAccessor,
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
