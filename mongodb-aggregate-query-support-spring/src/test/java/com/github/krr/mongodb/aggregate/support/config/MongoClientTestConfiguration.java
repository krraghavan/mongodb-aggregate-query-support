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

package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.config.conditions.NotUseRealMongoCondition;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rkolliva
 * 1/21/17.
 */

@Configuration
@Conditional(NotUseRealMongoCondition.class)
public class MongoClientTestConfiguration {

//  @Value("embedmongo.port")
//  private int mongoPort;

  @Bean
  public MongoClient mongo() {
//    ServerAddress address = new ServerAddress("localhost", mongoPort);
//    return MongoClients.create(MongoClientSettings.builder()
//                                                  .applyToClusterSettings(b -> b.hosts(singletonList(address)))
//                                                  .build());
    return MongoClients.create();
  }

}
