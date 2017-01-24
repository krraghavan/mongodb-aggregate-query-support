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

package com.cisco.mongodb.aggregate.support.test.config;

import com.cisco.mongodb.aggregate.support.test.config.conditions.UseRealMongoCondition;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import java.net.UnknownHostException;

/**
 * Created by rkolliva
 * 1/21/17.
 */

@Conditional(UseRealMongoCondition.class)
public class MongoClientConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientConfiguration.class);

  private static final int PORT = 27017;

  private static final int CONNECTION_CONNECTION_PER_HOST = 500;

  private static final int BLOCKING_THREAD_MULTIPLIER = 4;

  private static final int MAX_WAIT_TIME = 120 * 1000;

  private static final int MAX_CONNECTION_TIMEOUT = 30 * 1000;

  private static final int MAX_CONNECTION_IDLE_TIMEOUT = 30 * 1000;

  private static final int MAX_SOCKET_TIMEOUT = 5000;

  private static final String HOST = "127.0.0.1";

  @Bean
  public MongoClient mongo() throws UnknownHostException {
    LOGGER.info("Master MongoDB node is {}", HOST);
    WriteConcern writeConcern = WriteConcern.JOURNALED;
    MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
        .connectionsPerHost(CONNECTION_CONNECTION_PER_HOST)
        .threadsAllowedToBlockForConnectionMultiplier(BLOCKING_THREAD_MULTIPLIER)
        .maxWaitTime(MAX_WAIT_TIME)
        .connectTimeout(MAX_CONNECTION_TIMEOUT)
        .maxConnectionIdleTime(MAX_CONNECTION_IDLE_TIMEOUT)
        .socketKeepAlive(true)
        .socketTimeout(MAX_SOCKET_TIMEOUT)
        .writeConcern(writeConcern)
        .build();
    MongoClient mongoClient = new MongoClient(new ServerAddress(HOST, PORT), mongoClientOptions);
    LOGGER.info("Creating mongoClient with writeConcern " + mongoClient.getWriteConcern());
    return mongoClient;
  }

  @Bean
  public String dbName() {
    return "aggregateQueryTest";
  }
}
