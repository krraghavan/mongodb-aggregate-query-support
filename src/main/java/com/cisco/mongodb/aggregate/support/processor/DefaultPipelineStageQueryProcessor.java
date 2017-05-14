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

package com.cisco.mongodb.aggregate.support.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rkolliva
 * 4/2/17.
 */


public class DefaultPipelineStageQueryProcessor implements PipelineStageQueryProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPipelineStageQueryProcessor.class);

  protected Set<Integer> conditionalIndexes = new HashSet<>();

  public DefaultPipelineStageQueryProcessor() {
  }

  @Override
  public String getQuery(QueryProcessorContext context) {
    LOGGER.trace(">>>> DefaultPipelineStageQueryProcessor::getQuery");
    return context.getQuery();
  }

  @Override
  public int getOrder(QueryProcessorContext context) {
    Annotation annotation = context.getAnnotation();
    Method method;
    try {
      method = annotation.getClass().getDeclaredMethod("order");
      return (int) method.invoke(annotation);
    }
    catch (NoSuchMethodException e) {
      return -1;
    }
    catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalArgumentException("Could not determine order for query", e);
    }
  }

  @Override
  public Set<Integer> getConditionalIndexes() {
    return conditionalIndexes;
  }

}
