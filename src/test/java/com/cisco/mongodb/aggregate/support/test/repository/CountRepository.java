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

package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.annotation.Count;
import com.cisco.mongodb.aggregate.support.annotation.Match;
import com.cisco.mongodb.aggregate.support.test.beans.Score;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by rkolliva
 * 2/20/17.
 */


public interface CountRepository extends MongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, outputBeanType = Integer.class, resultKey = "passing_scores",
             match = {
                 @Match(query = "{\n" +
                                "        score: {\n" +
                                "          $gt: 80\n" +
                                "        }\n" +
                                "      }", order = 0)
             },
             count = {
                 @Count(query = "\"passing_scores\"", order = 1)
             }
  )
  Integer getPassingScores();
}
