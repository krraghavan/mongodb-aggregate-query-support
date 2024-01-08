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

package com.github.krr.mongodb.aggregate.support.repository.nonreactive;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.Possessions;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueFalseCondition;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueNotNullCondition;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueNullCondition;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueTrueCondition;
import com.mongodb.DBObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


/**
 * Created by rkolliva
 * 3/1/17.
 */
@SuppressWarnings("unused")
public interface PossessionsRepository extends MongoRepository<Possessions, String> {

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                 "   \"tag\": ?0}" +
                 "   }" +
                 "}", order = 0)
  List<Possessions> getPossessions(String tag);

  default boolean hasCars(String tag) {
    return CollectionUtils.isNotEmpty(getPossessions(tag));
  }

  default boolean hasHomes(String id) {
    return CollectionUtils.isNotEmpty(getPossessions("homes"));
  }

  @Aggregate(inputType = Possessions.class, outputBeanType = DBObject.class)
  @Match(query = "{" +
                 "   \"_id\": ?0" +
                 "}", order = 0)
  List<DBObject> getPossessionsDbObject(String id);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match(query = "{" +
                "   \"tag\": ?0" +
                "}", order = 0)
  @Sort(query = "\"@@1\"", order = 1)
  List<Possessions> getPossesionsSortedByTag(String tag, String sort);

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
  List<Possessions> mutuallyExclusiveStages(String tag, Boolean getCars, Boolean getHomes);

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
                "}", order = 1, condition = {
     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
  })
  @Sort(query = "{\"sortTestNumber\" : -1}", order = 2)
  List<Possessions> getWithMixOfConditionalAndUnconditionalStages(String tag, Boolean getCars, Boolean getHomes);

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
  List<Possessions> mutuallyExclusiveStagesPageable(String tag, Boolean getCars, Boolean getHomes, Pageable pageable);

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
  List<Possessions> getPossessionsWithPotentiallyNullIdList(String tag, List<String> ids);

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
  List<Possessions> getPosessionsBasedOnConditionType(String tag, boolean condition1, boolean condition2);
}
