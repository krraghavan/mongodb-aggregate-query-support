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

package com.github.krr.mongodb.aggregate.support.processor;

import com.github.krr.mongodb.aggregate.support.annotations.Facet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rkolliva
 * 4/2/17.
 */
public class DefaultPipelineStageQueryProcessorFactory implements PipelineStageQueryProcessorFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPipelineStageQueryProcessorFactory.class);

  @Override
  public PipelineStageQueryProcessor getQueryProcessor(QueryProcessorContext context) {
    LOGGER.trace(">>>> DefaultPipelineStageQueryProcessorFactory::getQueryProcessor");
    if(context.getAnnotation().annotationType() == Facet.class) {
      return new Facet2PipelineStageQueryProcessor();
    }
    LOGGER.trace("<<<< DefaultPipelineStageQueryProcessorFactory::getQueryProcessor");
    return new DefaultPipelineStageQueryProcessor();
  }
}
