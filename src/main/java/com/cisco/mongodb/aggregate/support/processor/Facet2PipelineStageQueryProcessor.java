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

import com.cisco.mongodb.aggregate.support.annotation.Conditional;
import com.cisco.mongodb.aggregate.support.annotation.v2.Facet2;
import com.cisco.mongodb.aggregate.support.annotation.v2.FacetPipeline;
import com.cisco.mongodb.aggregate.support.annotation.v2.FacetPipelineStage;
import com.cisco.mongodb.aggregate.support.condition.AggregateQueryMethodConditionContext;
import com.cisco.mongodb.aggregate.support.condition.ConditionalAnnotationMetadata;
import com.cisco.mongodb.aggregate.support.query.AbstractAggregateQueryProvider;
import com.cisco.mongodb.aggregate.support.query.AbstractAggregateQueryProvider.AggregationType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.List;

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

  public Facet2PipelineStageQueryProcessor() {
    //
  }

  @Override
  public String getQuery(QueryProcessorContext context) {
    Annotation annotation = context.getAnnotation();
    Assert.isAssignable(Facet2.class, annotation.annotationType(), "Annotation must be facet2 type");
    Facet2 facet2 = (Facet2)annotation;
    StringBuilder stringBuilder = new StringBuilder();
    // process each pipeline.
    // if this annotation is to be processed - evaluate conditional
    Conditional [] conditionals = facet2.condition();
    boolean shouldProcessFacet = shouldProcessStage(context, conditionals);
    if(shouldProcessFacet) {
      FacetPipeline[] pipelines = facet2.pipelines();
      stringBuilder.append(OPEN_BRACE);
      AbstractAggregateQueryProvider queryProvider = (AbstractAggregateQueryProvider) context.queryProvider();
      int pipelineCount = 0;
      for(FacetPipeline pipeline : pipelines) {
        if(shouldProcessStage(context, pipeline.condition())) {
          processFacetPipelineStage(context, stringBuilder, queryProvider, pipelineCount, pipeline);
          pipelineCount++;
        }
      }
      stringBuilder.append(CLOSE_BRACE);
      String ret = String.format("{$facet:%s}", stringBuilder.toString());
      LOGGER.debug("Facet query {}", ret);
      return ret;
    }
    return null;
  }

  private void processFacetPipelineStage(QueryProcessorContext context, StringBuilder stringBuilder,
                                         AbstractAggregateQueryProvider queryProvider, int pipelineCount,
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

  private boolean shouldProcessStage(QueryProcessorContext context, Conditional[] conditionals) {
    boolean shouldProcess = true;
    for(Conditional conditional : conditionals) {
      try {
        Condition condition = conditional.condition().newInstance();
        List<Object> parameterValues = context.getAggregationStage().getParameterValues();
        ConditionalAnnotationMetadata metadata = new ConditionalAnnotationMetadata(conditional);
        AggregateQueryMethodConditionContext ctx = new AggregateQueryMethodConditionContext(context.getMethod(),
                                                                                            parameterValues);

        shouldProcess &= condition.matches(ctx, metadata);
      }
      catch (InstantiationException | IllegalAccessException e) {
        LOGGER.error("Condition class must have default constructor", e);
      }
    }
    return shouldProcess;
  }

  private void processFacetPipeline(QueryProcessorContext context, StringBuilder stringBuilder,
                                    AbstractAggregateQueryProvider queryProvider, FacetPipeline pipeline) {
    FacetPipelineStage[] pipelineStages = pipeline.stages();
    int count = 0;
    openFacetPipeline(stringBuilder);
    String name = pipeline.name();
    LOGGER.debug("Building pipeline for {}", name);
    for (FacetPipelineStage pipelineStage : pipelineStages) {
      if(shouldProcessStage(context, pipelineStage.condition())) {
        if (count++ != 0) {
          stringBuilder.append(COMMA);
        }
        Class<? extends Annotation> stageType = pipelineStage.stageType();
        AggregationType type = AggregationType.from(stageType);
        AbstractAggregateQueryProvider.AggregationStage stage = queryProvider.new AggregationStage(type,
                                                                                                   pipelineStage
                                                                                                       .condition());
        ParameterPlaceholderReplacingContext sctx = new ParameterPlaceholderReplacingContext(context, stage,
                                                                                             pipelineStage);
        String stageQueryString = super.getQuery(sctx);
        LOGGER.debug("Facet stage #[{}], query:{}", count, stageQueryString);
        stringBuilder.append(stageQueryString);
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
