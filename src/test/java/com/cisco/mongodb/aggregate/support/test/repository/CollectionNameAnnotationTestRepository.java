/*
 *  Copyright (c) 2018 the original author or authors.
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

package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.v2.*;
import com.cisco.mongodb.aggregate.support.test.beans.Artwork;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rkolliva
 * 1/2/18.
 */

public interface CollectionNameAnnotationTestRepository extends MongoRepository<Artwork, Integer> {

  @Aggregate2(inputType = Artwork.class, genericType = true, outputBeanType = HashMap.class)
  @Facet2(pipelines = {
      @FacetPipeline(name = "categorizedByTags",
                     stages = {
                         @FacetPipelineStage(stageType = Unwind2.class, query = "'$tags'"),
                         @FacetPipelineStage(stageType = SortByCount.class, query = "'$tags'")
                     }),
      @FacetPipeline(name = "categorizedByPrice",
                     stages = {
                         @FacetPipelineStage(stageType = Match2.class, query = "{ price: { $exists: 1 } }"),
                         @FacetPipelineStage(stageType = Bucket2.class, query = "{" +
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
  Map<String, Object> getArtworkFromDynamicCollection(@CollectionName String collName);

}
