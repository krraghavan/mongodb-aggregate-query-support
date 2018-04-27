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

/**
 * Created by rkolliva
 * 4/2/17.
 */
public interface PipelineStageQueryProcessorFactory {

  /**
   * Implementations return a query processor that can return the query string
   * for a given annotation.  The context
   *
   * @param context - A runtime context that gives access to the AggregationStage
   *                and a query string.
   * @return The pipelineStage query processor that can be used to return a query
   * string for that pipeline stage.
   *
   */
  PipelineStageQueryProcessor getQueryProcessor(QueryProcessorContext context);
}
