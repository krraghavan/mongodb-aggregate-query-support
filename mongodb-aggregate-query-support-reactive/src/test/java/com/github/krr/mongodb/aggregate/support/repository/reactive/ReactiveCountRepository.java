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

package com.github.krr.mongodb.aggregate.support.repository.reactive;

import com.github.krr.mongodb.aggregate.support.CountGt75AggregateAnnotationsContainer;
import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import org.bson.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Created by rkolliva
 * 2/20/17.
 */


public interface ReactiveCountRepository extends ReactiveMongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, maxTimeMS = 60_000L, outputBeanType = Integer.class, resultKey = "passing_scores")
  @Match(query = "{\n" +
                 "        score: {\n" +
                 "          $gt: 80\n" +
                 "        }\n" +
                 "      }", order = 0)
  @Count(query = "\"passing_scores\"", order = 1)
  Mono<Integer> getPassingScores2();

  @Aggregate(inputType = Score.class, maxTimeMS = 60_000L, outputBeanType = Integer.class, resultKey = "passing_scores")
  @Match(query = "{\n" +
                 "        score: {\n" +
                 "          $gt: 80\n" +
                 "        }\n" +
                 "      }", order = 0)
  @Count(query = "\"passing_scores\"", order = 1)
  Mono<Integer> getPassingScores2FromSpecifiedCollection(@CollectionName String collName);

  @Aggregate(inputType = Score.class, outputBeanType = Integer.class, resultKey = "scores_gt_75")
  @CountGt75AggregateAnnotationsContainer
  Mono<Integer> scoresGreaterThan75UsingMetaAnnotation();

  @Aggregate(inputType = Score.class, maxTimeMS = 60_000L)
  @Match(query = "{\n" +
                 "        score: {\n" +
                 "          $gt: 80\n" +
                 "        }\n" +
                 "      }", order = 0)
  @Count(query = "\"passing_scores\"", order = 1)
  Mono<Document> invalidGetPassingScores2FromSpecifiedCollection(@CollectionName String collName, Object randomParam,
                                                                @CollectionName String collName2);

  @Aggregate(inputType = Score.class, maxTimeMS = 60_000L)
  @Match(query = "{\n" +
                 "        score: {\n" +
                 "          $gt: 80\n" +
                 "        }\n" +
                 "      }", order = 0)
  @Count(query = "\"passing_scores\"", order = 1)
  Mono<Document> invalidGetPassingScores2FromSpecifiedCollection2(@CollectionName String collName, Object randomParam,
                                                                 @CollectionName @TestNotNull String collName2);
}
