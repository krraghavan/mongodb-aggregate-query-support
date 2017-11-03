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
import com.cisco.mongodb.aggregate.support.annotation.Conditional;
import com.cisco.mongodb.aggregate.support.annotation.Match;
import com.cisco.mongodb.aggregate.support.annotation.Sort;
import com.cisco.mongodb.aggregate.support.annotation.v2.Aggregate2;
import com.cisco.mongodb.aggregate.support.annotation.v2.Limit2;
import com.cisco.mongodb.aggregate.support.annotation.v2.Match2;
import com.cisco.mongodb.aggregate.support.condition.*;
import com.cisco.mongodb.aggregate.support.test.beans.Possessions;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import static com.cisco.mongodb.aggregate.support.annotation.Conditional.*;

/**
 * Created by rkolliva
 * 3/1/17.
 */
public interface PossessionsRepository extends MongoRepository<Possessions, String> {

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\": ?0}" +
                                "   }" +
                                "}", order = 0)
             })
  List<Possessions> getPossessions(String tag);

  default boolean hasCars(String tag) {
    return CollectionUtils.isNotEmpty(getPossessions( tag));
  }

  default boolean hasHomes(String id) {
    return CollectionUtils.isNotEmpty(getPossessions( "homes"));
  }

  @Aggregate(inputType = Possessions.class, outputBeanType = DBObject.class,
             match = {
                 @Match(query = "{" +
                                "   \"_id\": ?0" +
                                "}", order = 0)
             })
  List<DBObject> getPossessionsDbObject(String id);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\": ?0" +
                                "}", order = 0)
             },
             sort = {
                 @Sort(query = "\"@@1\"", order = 1)
             })
  List<Possessions> getPossesionsSortedByTag(String tag, String sort);

  @Aggregate2(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match2(query = "{" +
                                 "   \"tag\"  : ?0," +
                                 "   \"assets.cars\" : { $exists: true, $ne : []}" +
                                 "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)}
                 )
                 @Match2(query = "{" +
                                "   \"tag\": ?0," +
                                "   \"assets.homes\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
                 })
  List<Possessions> mutuallyExclusiveStages(String tag, Boolean getCars, Boolean getHomes);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\"  : ?0," +
                                "   \"assets.cars\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
                 }),
                 @Match(query = "{" +
                                "   \"tag\": ?0," +
                                "   \"assets.homes\" : { $exists: true, $ne : []}" +
                                "}", order = 1, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
                 })
             },
             sort = {
                 @Sort(query = "{\"sortTestNumber\" : -1}", order = 2)
             })
  List<Possessions> getWithMixOfConditionalAndUnconditionalStages(String tag, Boolean getCars, Boolean getHomes);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\"  : ?0," +
                                "   \"assets.cars\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
                 }),
                 @Match(query = "{" +
                                "   \"tag\": ?0," +
                                "   \"assets.homes\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
                 })
             })
  List<Possessions> mutuallyExclusiveStagesPageable(String tag, Boolean getCars, Boolean getHomes, Pageable pageable);

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
          match = {
                  @Match(query = "{" +
                          "   'tag': ?0," +
                          "   '_id': { $in : ?1 }" +
                          "}", order = 0, condition = {
                      @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
                  }),
                  @Match(query = "{" +
                          "   'tag': ?0" +
                          "}", order = 0, condition = {
                      @Conditional(condition = ParameterValueNullCondition.class, parameterIndex = 1)
                  })
          },
          sort = {
                  @Sort(query = "{\"sortTestNumber\" : -1}", order = 1)
          })
  List<Possessions> getPossessionsWithPotentiallyNullIdList(String tag, List<String> ids);

  @Aggregate2(inputType = Possessions.class, outputBeanType = Possessions.class)
  @Match2(order = 0, query = "{'tag' : ?0}")
  @Limit2(order = 1, query = "4", conditionMatchType = ConditionalMatchType.ANY,
          condition = {
             @Conditional(condition = ParameterValueTrueCondition.class, parameterIndex = 1),
             @Conditional(condition = ParameterValueTrueCondition.class, parameterIndex = 2)
          })
  @Limit2(order = 2, query = "8", conditionMatchType = ConditionalMatchType.ALL,
      condition = {
          @Conditional(condition = ParameterValueFalseCondition.class, parameterIndex = 1),
          @Conditional(condition = ParameterValueFalseCondition.class, parameterIndex = 2)
      })
  List<Possessions> getPosessionsBasedOnConditionType(String tag, boolean condition1, boolean condition2);
}
