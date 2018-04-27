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

import com.github.krr.mongodb.aggregate.support.annotations.Conditional;
import com.github.krr.mongodb.aggregate.support.annotations.Facet;
import com.github.krr.mongodb.aggregate.support.annotations.FacetPipeline;
import com.github.krr.mongodb.aggregate.support.annotations.FacetPipelineStage;
import com.github.krr.mongodb.aggregate.support.api.QueryProvider;
import com.github.krr.mongodb.aggregate.support.enums.AggregationType;
import com.github.krr.mongodb.aggregate.support.utils.Assert;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

import static com.github.krr.mongodb.aggregate.support.query.AbstractAggregateQueryProvider.AggregationStage;
import static com.github.krr.mongodb.aggregate.support.utils.ArrayUtils.NULL_STRING;

//import com.github.krr.mongodb.aggregate.support.query.AbstractAggregateQueryProviderReactive;
//import com.github.krr.mongodb.aggregate.support.utils.ProcessorUtils;

/**
 * Created by rkolliva
 * 4/2/17.
 */


@SuppressWarnings({"WeakerAccess", "squid:S134"})
public class Facet2PipelineStageQueryProcessor extends DefaultPipelineStageQueryProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Facet2PipelineStageQueryProcessor.class);

  private static final String QUOTE = "\"";
  private static final String COLON = ":";
  private static final String OPEN_BRACKET = "[";
  private static final String OPEN_BRACE = "{";
  private static final String CLOSE_BRACE = "}";
  private static final String CLOSE_BRACKET = "]";
  private static final String COMMA = ",";

//  private ProcessorUtils processorUtils;

  public Facet2PipelineStageQueryProcessor() {
    //
  }

  @Override
  public String getQuery(QueryProcessorContext context) {
    Annotation annotation = context.getAnnotation();
    Assert.isAssignable(Facet.class, annotation.annotationType(), "Annotation must be Facet type");
    Facet facet2 = (Facet)annotation;
    StringBuilder stringBuilder = new StringBuilder();
    // process each pipeline.
    // if this annotation is to be processed - evaluate conditional
    Conditional[] conditionals = facet2.condition();
    boolean shouldProcessFacet = context.getAggregationStage().allowStage();
    if(shouldProcessFacet) {
      FacetPipeline[] pipelines = facet2.pipelines();
      stringBuilder.append(OPEN_BRACE);
      QueryProvider queryProvider = context.queryProvider();
      int pipelineCount = 0;
      for(FacetPipeline pipeline : pipelines) {
        if(context.getAggregationStage().allowStage(pipeline.condition(), pipeline.conditionMatchType())) {
          processFacetPipelineStage(context, stringBuilder, queryProvider, pipelineCount, pipeline);
          pipelineCount++;
        }
      }
      stringBuilder.append(CLOSE_BRACE);
      String ret = String.format("{$facet:%s}", stringBuilder.toString());
      LOGGER.debug("Facet query {}", ret);
      return ret;
    }
    return NULL_STRING;
  }

  private void processFacetPipelineStage(QueryProcessorContext context,
                                         StringBuilder stringBuilder,
                                         QueryProvider queryProvider,
                                         int pipelineCount,
                                         FacetPipeline pipeline) {
    addFacetPipelineName(stringBuilder, pipelineCount, pipeline);
    String pipelineStagQuery = pipeline.query();
    if (!StringUtils.isEmpty(pipelineStagQuery)) {
      LOGGER.debug("Appending raw query string {} for pipeline stage");
      if (ArrayUtils.isNotEmpty(pipeline.stages())) {
        String pipelineName = pipeline.name();
        LOGGER.warn("Both query() and stages() are specified for {}.  query string is used and stages is ignored",
                    pipelineName);
      }
      stringBuilder.append(pipelineStagQuery);
    }
    else {
      processFacetPipeline(context, stringBuilder, queryProvider, pipeline);
    }
  }

  private void processFacetPipeline(QueryProcessorContext context, StringBuilder stringBuilder,
                                    QueryProvider queryProvider, FacetPipeline pipeline) {
    FacetPipelineStage[] pipelineStages = pipeline.stages();
    int count = 0;
    openFacetPipeline(stringBuilder);
    String name = pipeline.name();
    LOGGER.debug("Building pipeline for {}", name);
    for (FacetPipelineStage pipelineStage : pipelineStages) {
      if(context.getAggregationStage().allowStage(pipelineStage.condition(), pipelineStage.conditionMatchType())) {
        Class<? extends Annotation> stageType = pipelineStage.stageType();
        AggregationType type = AggregationType.from(stageType);
        AggregationStage stage = queryProvider.createAggregationStage(type, pipelineStage.condition(),
                                                                      pipelineStage.conditionMatchType());
        Assert.notNull(stage, "Expecting non null AggregationStage");
        ParameterPlaceholderReplacingContext sctx = new ParameterPlaceholderReplacingContext(context, stage,
                                                                                             pipelineStage);
        String stageQueryString = super.getQuery(sctx);
        LOGGER.debug("Facet stage #[{}], query:{}", count, stageQueryString);
        if(!NULL_STRING.equals(stageQueryString)) {
          if (count++ != 0) {
            stringBuilder.append(COMMA);
          }
          stringBuilder.append(stageQueryString);
        }
      }
    }
    closeFacetPipeline(stringBuilder);
  }

  private void addFacetPipelineName(StringBuilder stringBuilder, int pipelineCount, FacetPipeline pipeline) {
    if (pipelineCount > 0) {
      stringBuilder.append(COMMA);
    }
    String facetName = pipeline.name();
    stringBuilder.append(QUOTE).append(facetName).append(QUOTE).append(COLON);
  }

  private void openFacetPipeline(StringBuilder stringBuilder) {
    stringBuilder.append(OPEN_BRACKET);
  }

  private void closeFacetPipeline(StringBuilder stringBuilder) {
    stringBuilder.append(CLOSE_BRACKET);
  }
}
