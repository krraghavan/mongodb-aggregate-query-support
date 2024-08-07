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

package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.annotations.Conditional.ConditionalMatchType;
import com.github.krr.mongodb.aggregate.support.beans.Artwork;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueFalseCondition;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueTrueCondition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rkolliva
 * 2/19/17.
 */
public interface ArtworkRepository extends MongoRepository<Artwork, Integer> {

  @Aggregate(inputType = Artwork.class, outputBeanType = HashMap.class)
  @Facet(pipelines = {
      @FacetPipeline(name = "categorizedByTags",
          stages = {
              @FacetPipelineStage(stageType = Unwind.class, query = "'$tags'"),
              @FacetPipelineStage(stageType = SortByCount.class, query = "'$tags'")
          }),
      @FacetPipeline(name = "categorizedByPrice",
          stages = {
              @FacetPipelineStage(stageType = Match.class, query = "{ price: { $exists: 1 } }"),
              @FacetPipelineStage(stageType = Bucket.class, query = "{" +
                                                                    "  groupBy: \"$price\",\n" +
                                                                    "  boundaries: [  0, 150, 200, 300, 400 ],\n" +
                                                                    "  default: \"Other\",\n" +
                                                                    "  output: {\n" +
                                                                    "  \"count\": { $sum: 1 },\n" +
                                                                    "  \"titles\": { $push: \"$title\" }\n" +
                                                                    "  }\n" +
                                                                    "}")
          })}
      , order = 0)
  Map<String, Object> getFacetResults2();

  // fixture created for bug: https://github.com/krraghavan/mongodb-aggregate-query-support/issues/50
  @Aggregate(inputType = Artwork.class)
  @Facet(pipelines = {
      @FacetPipeline(name = "categorizedByTags",
          stages = {
              @FacetPipelineStage(stageType = Skip.class, query = "?0"),
              @FacetPipelineStage(stageType = Limit.class, query = "?1"),
              @FacetPipelineStage(stageType = Project.class, query = "@@2")
          })}, order = 0)
  Map<String, Object> facetPipelineStageWithAtAtPh(Long arg1, Long arg2, String arg3);

  @Aggregate(inputType = Artwork.class, outputBeanType = HashMap.class)
  @Facet(pipelines = {
      @FacetPipeline(name = "categorizedByTags",
          stages = {
              @FacetPipelineStage(stageType = Unwind.class, query = "'$tags'"),
              @FacetPipelineStage(stageType = SortByCount.class, query = "'$tags'")
          }),
      @FacetPipeline(name = "categorizedByPrice",
          stages = {
              @FacetPipelineStage(stageType = Match.class, query = "{ price: { $exists: 1 } }"),
              @FacetPipelineStage(stageType = Bucket.class, query = "{" +
                                                                    "  groupBy: \"$price\",\n" +
                                                                    "  boundaries: [  0, 150, 200, 300, 400 ],\n" +
                                                                    "  default: \"Other\",\n" +
                                                                    "  output: {\n" +
                                                                    "  \"count\": { $sum: 1 },\n" +
                                                                    "  \"titles\": { $push: \"$title\" }\n" +
                                                                    "  }\n" +
                                                                    "}")
          })}
      , order = 0)
  @Facet(pipelines = {
      @FacetPipeline(name = "count",
          stages = {
              @FacetPipelineStage(stageType = Count.class, query = "'resultSetCount'")
          })
  }
      , order = 1)
  Map<String, Object> getFacetResultsWithMultipleFacets();

  @Aggregate(inputType = Artwork.class, outputBeanType = HashMap.class)
  @Facet(pipelines = {
      @FacetPipeline(name = "categorizedByTags",
          stages = {
              @FacetPipelineStage(stageType = Unwind.class, query = "'$tags'"),
              @FacetPipelineStage(stageType = SortByCount.class, query = "'$tags'")
          }, condition = {
          @Conditional(condition = ParameterValueTrueCondition.class, parameterIndex = 0)
      }),
      @FacetPipeline(name = "categorizedByPrice",
          stages = {
              @FacetPipelineStage(stageType = Match.class, query = "{ price: { $exists: 1 } }"),
              @FacetPipelineStage(stageType = Bucket.class, query = "{" +
                                                                    "  groupBy: \"$price\",\n" +
                                                                    "  boundaries: [  0, 150, 200, 300, 400 ],\n" +
                                                                    "  default: \"Other\",\n" +
                                                                    "  output: {\n" +
                                                                    "  \"count\": { $sum: 1 },\n" +
                                                                    "  \"titles\": { $push: \"$title\" }\n" +
                                                                    "  }\n" +
                                                                    "}"),
              @FacetPipelineStage(stageType = Limit.class, query = "2",
                  condition = {
                      @Conditional(condition = ParameterValueTrueCondition.class,
                          parameterIndex = 1)
                  }),
              @FacetPipelineStage(stageType = Limit.class, query = "3",
                  condition = {
                      @Conditional(condition = ParameterValueTrueCondition.class,
                          parameterIndex = 2)
                  })
          })}
      , order = 0)
  Map<String, Object> getFacetResultsWithConditional(Boolean showTags, Boolean limit2, Boolean limit3);

  default Map<String, Object> getFacetResultsWithConditional(Boolean showTags) {
    return getFacetResultsWithConditional(showTags, false, false);
  }

  @Aggregate(inputType = Artwork.class, outputBeanType = HashMap.class)
  @Facet(pipelines = {
      @FacetPipeline(name = "limitPipeline",
          stages = {
              @FacetPipelineStage(stageType = Limit.class, query = "2", conditionMatchType = ConditionalMatchType.ANY,
                  condition = {
                      @Conditional(condition = ParameterValueTrueCondition.class, parameterIndex = 0),
                      @Conditional(condition = ParameterValueTrueCondition.class, parameterIndex = 1)
                  }),
              @FacetPipelineStage(stageType = Limit.class, query = "3", conditionMatchType = ConditionalMatchType.ALL,
                  condition = {
                      @Conditional(condition = ParameterValueFalseCondition.class, parameterIndex = 0),
                      @Conditional(condition = ParameterValueFalseCondition.class, parameterIndex = 1)
                  })
          }
      )
  }, order = 0)
  Map<String, Object> getFacetResultsOnMultipleConditionals(Boolean condition1, Boolean condition2);

}

