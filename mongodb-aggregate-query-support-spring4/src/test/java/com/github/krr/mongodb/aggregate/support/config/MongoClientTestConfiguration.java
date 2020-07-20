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
import com.github.krr.mongodb.embeddedmongo.config.Mongo42xDownloadConfigBuilder;
import com.github.krr.mongodb.embeddedmongo.config.MongoDbVersion;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.runtime.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by rkolliva
 * 1/21/17.
 */

@Configuration
@Conditional(NotUseRealMongoCondition.class)
public class MongoClientTestConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientTestConfiguration.class);

  private static final String LOCALHOST = "localhost";
  private static final String V4_2_5 = "4.2.5";

  private final MongodExecutable mongodExecutable;

  private MongodProcess mongodProcess;

  public MongoClientTestConfiguration() throws IOException {

    Command command = Command.MongoD;
    IDownloadConfig downloadConfig = new Mongo42xDownloadConfigBuilder().defaultsForCommand(command)
                                                                        .progressListener(new Slf4jProgressListener(LOGGER))
                                                                        .build();
    IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(command)
                                                             .artifactStore(new ExtractedArtifactStoreBuilder()
                                                                                .defaults(command)
                                                                                .download(downloadConfig))
                                                             .build();
    final MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
    mongodExecutable = runtime.prepare(newMongodConfig(new MongoDbVersion(V4_2_5)));
    startMongodExecutable();
  }

  private void startMongodExecutable() throws IOException {
    mongodProcess = mongodExecutable.start();
  }

  private IMongodConfig newMongodConfig(final IFeatureAwareVersion version) throws IOException {
    MongoCmdOptionsBuilder builder = new MongoCmdOptionsBuilder().useSmallFiles(false).useNoPrealloc(false);
    return new MongodConfigBuilder().version(version)
                                    .cmdOptions(builder.build())
                                    .net(new Net(LOCALHOST, Network.getFreeServerPort(),
                                                 Network.localhostIsIPv6())).build();
  }

  @Bean
  public MongoClient mongo() throws IOException {
    //individual unit test is running, likely in intelliJ
    return getMongoClient();
  }

  private MongoClient getMongoClient() throws IOException {
      return new MongoClient(new ServerAddress(mongodProcess.getConfig().net().getServerAddress(),
                                               mongodProcess.getConfig().net().getPort()));
  }

  @Bean
  public MongoClient mongoForJongo() throws IOException {
    return getMongoClient();
  }

  @PreDestroy
  public void tearDown() {
    if (mongodExecutable != null) {
      mongodExecutable.stop();
    }
    if (mongodProcess != null) {
      mongodProcess.stop();
    }
  }

  private boolean isPortInUse(String host, int port) throws IOException {
    // Assume no connection is possible.
    boolean result = false;

    try {
      (new Socket(host, port)).close();
      result = true;
    }
    catch(SocketException e) {
      // Could not connect.
    }

    return result;
  }
}
