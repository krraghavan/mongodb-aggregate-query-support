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
public interface PipelineStageQueryProcessor {

  /**
   * Returns the query string for the pipeline stage.
   *
   * @param context - a runtime context used to return the query
   *
   * @return - A string representing the query for the pipeline stage.
   */
  String getQuery(QueryProcessorContext context);

  int getOrder(QueryProcessorContext context);
}
