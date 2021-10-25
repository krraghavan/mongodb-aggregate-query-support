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
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import de.flapdoodle.embed.mongo.MongodProcess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.UnknownHostException;

import static com.mongodb.connection.ClusterType.STANDALONE;
import static java.util.Collections.singletonList;

/**
 * Created by rkolliva
 * 4/18/18
 */
@Configuration
public class NonReactiveMongoClientTestConfiguration {

  private final MongodProcess mongodProcess;

  private static MongoTestServerConfiguration serverConfiguration = null;

  static {
    try {
      serverConfiguration = new MongoTestServerConfiguration();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public NonReactiveMongoClientTestConfiguration() {
    if (serverConfiguration == null) {
      throw new IllegalStateException("No Server configuration created.  Cannot continue...");
    }
    this.mongodProcess = serverConfiguration.mongodProcess();
  }

  @Bean
  public MongoClient mongoClient() throws IOException {
    ServerAddress serverAddress = getServerAddress();
    ClusterSettings clusterSettings = ClusterSettings.builder()
                                                                    .hosts(singletonList(serverAddress))
                                                                    .requiredClusterType(STANDALONE).build();
    MongoClientSettings settings = MongoClientSettings.builder()
                                                      .applyToClusterSettings(c -> c.applySettings(clusterSettings)).build();
    return MongoClients.create(settings);

  }

  private ServerAddress getServerAddress() throws UnknownHostException {
    return new ServerAddress(mongodProcess.getConfig().net().getServerAddress(),
                             mongodProcess.getConfig().net().getPort());
  }

  @PreDestroy
  public void tearDown() {
    serverConfiguration.tearDown();
  }

}
