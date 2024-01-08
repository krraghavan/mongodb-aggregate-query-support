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

package com.github.krr.mongodb.aggregate.support.condition;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by rkolliva
 * 3/7/17.
 */


public class AggregateQueryMethodConditionContext implements ConditionContext {

  private final List<Object> parameterValues;

  private final Method method;

  public AggregateQueryMethodConditionContext(Method aggQueryMethod, List<Object> parameterValues) {
    this.parameterValues = parameterValues;
    this.method = aggQueryMethod;
  }

  @Override
  public BeanDefinitionRegistry getRegistry() {
    return null;
  }

  @Override
  public ConfigurableListableBeanFactory getBeanFactory() {
    return null;
  }

  @Override
  public Environment getEnvironment() {
    return null;
  }

  @Override
  public ResourceLoader getResourceLoader() {
    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    return null;
  }

  public List<Object> getParameterValues() {
    return parameterValues;
  }

  public Method getMethod() {
    return method;
  }
}
