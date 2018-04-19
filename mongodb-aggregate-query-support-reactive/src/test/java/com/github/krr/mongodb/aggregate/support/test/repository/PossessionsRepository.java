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

package com.github.krr.mongodb.aggregate.support.test.repository;

import com.github.krr.mongodb.aggregate.support.annotation.*;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueFalseCondition;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueNotNullCondition;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueNullCondition;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueTrueCondition;
import com.github.krr.mongodb.aggregate.support.test.beans.Possessions;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by rkolliva
 * 3/1/17.
 */
public interface PossessionsRepository extends ReactiveMongoRepository<Possessions, String> {

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                 "   \"tag\": ?0}" +
                 "   }" +
                 "}", order = 0)
  Flux<Possessions> getPossessions(String tag);

  default boolean hasCars(String tag) {
    return getPossessions(tag).count().block() > 0;
  }

  default boolean hasHomes(String id) {
    return getPossessions("homes").count().block() > 0;
  }

  @Aggregate(inputType = Possessions.class)
  @Match(query = "{" +
                 "   \"_id\": ?0" +
                 "}", order = 0)
  Mono<Document> getPossessionsDbObject(String id);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                 "   \"tag\": ?0" +
                 "}", order = 0)
  @Sort(query = "\"@@1\"", order = 1)
  Flux<Possessions> getPossesionsSortedByTag(String tag, String sort);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                 "   \"tag\"  : ?0," +
                 "   \"assets.cars\" : { $exists: true, $ne : []}" +
                 "}", order = 0, condition = {
      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)}
  )
  @Match(query = "{" +
                 "   \"tag\": ?0," +
                 "   \"assets.homes\" : { $exists: true, $ne : []}" +
                 "}", order = 0, condition = {
      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
  })
  Flux<Possessions> mutuallyExclusiveStages(String tag, Boolean getCars, Boolean getHomes);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                 "   \"tag\"  : ?0," +
                 "   \"assets.cars\" : { $exists: true, $ne : []}" +
                 "}", order = 0, condition = {
      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)})
  @Match(query = "{" +
                 "   \"tag\": ?0," +
                 "   \"assets.homes\" : { $exists: true, $ne : []}" +
                 "}", order = 1, condition = {
      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
  })
  @Sort(query = "{\"sortTestNumber\" : -1}", order = 2)
  Flux<Possessions> getWithMixOfConditionalAndUnconditionalStages(String tag, Boolean getCars, Boolean getHomes);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                 "   \"tag\"  : ?0," +
                 "   \"assets.cars\" : { $exists: true, $ne : []}" +
                 "}", order = 0, condition = {
      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
  })
  @Match(query = "{" +
                 "   \"tag\": ?0," +
                 "   \"assets.homes\" : { $exists: true, $ne : []}" +
                 "}", order = 0, condition = {
      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
  })
  Flux<Possessions> mutuallyExclusiveStagesPageable(String tag, Boolean getCars, Boolean getHomes, Pageable pageable);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                 "   'tag': ?0," +
                 "   '_id': { $in : ?1 }" +
                 "}", order = 0, condition = {
      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
  })
  @Match(query = "{" +
                 "   'tag': ?0" +
                 "}", order = 0, condition = {
      @Conditional(condition = ParameterValueNullCondition.class, parameterIndex = 1)
  })
  @Sort(query = "{\"sortTestNumber\" : -1}", order = 1)
  Flux<Possessions> getPossessionsWithPotentiallyNullIdList(String tag, List<String> ids);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(order = 0, query = "{'tag' : ?0}")
  @Limit(order = 1, query = "4", conditionMatchType = Conditional.ConditionalMatchType.ANY,
         condition = {
              @Conditional(condition = ParameterValueTrueCondition.class, parameterIndex = 1),
              @Conditional(condition = ParameterValueTrueCondition.class, parameterIndex = 2)
          })
  @Limit(order = 2, query = "8", conditionMatchType = Conditional.ConditionalMatchType.ALL,
         condition = {
              @Conditional(condition = ParameterValueFalseCondition.class, parameterIndex = 1),
              @Conditional(condition = ParameterValueFalseCondition.class, parameterIndex = 2)
          })
  Flux<Possessions> getPosessionsBasedOnConditionType(String tag, boolean condition1, boolean condition2);
}
