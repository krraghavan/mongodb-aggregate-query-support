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

import com.github.krr.mongodb.embeddedmongo.config.MongoTestServerConfiguration;
import com.github.krr.mongodb.embeddedmongo.config.conditions.NotUseRealMongoCondition;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.MongodProcess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Created by rkolliva
 * 1/21/17.
 */

@Configuration
@Conditional(NotUseRealMongoCondition.class)
public class MongoClientTestConfiguration {

  private final MongodProcess mongodProcess;

  private static MongoTestServerConfiguration serverConfiguration;

  static {
    try {
      serverConfiguration = new MongoTestServerConfiguration();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public MongoClientTestConfiguration() {
    if(serverConfiguration == null) {
      throw new IllegalStateException("No Server configuration created.  Cannot continue...");
    }
    mongodProcess = serverConfiguration.mongodProcess();
  }

  @Bean
  public MongoClient mongo() throws IOException {
    return new MongoClient(new ServerAddress(mongodProcess.getConfig().net().getServerAddress(),
                                             mongodProcess.getConfig().net().getPort()));
  }

  @PreDestroy
  public void tearDown() {
    serverConfiguration.tearDown();
  }
}
