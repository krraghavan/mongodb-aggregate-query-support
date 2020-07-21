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

import com.github.krr.mongodb.embeddedmongo.config.Mongo42xDownloadConfigBuilder;
import com.github.krr.mongodb.embeddedmongo.config.MongoDbVersion;
import com.github.krr.mongodb.embeddedmongo.config.TestLinuxPackageIoUtil;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
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
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.UnknownHostException;

import static com.github.krr.mongodb.embeddedmongo.config.LinuxDistributionReader.UBUNTU_1604;
import static com.mongodb.connection.ClusterType.STANDALONE;
import static java.util.Collections.singletonList;

/**
 * Created by rkolliva
 * 4/18/18
 */
@Configuration
public class ReactiveMongoClientTestConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveMongoClientTestConfiguration.class);

  private static final String LOCALHOST = "localhost";
  private static final String V4_2_5 = "4.2.5";

  private final MongodExecutable mongodExecutable;

  private MongodProcess mongodProcess;

  public ReactiveMongoClientTestConfiguration() throws IOException {

    Command command = Command.MongoD;
    TestLinuxPackageIoUtil ioUtil = TestLinuxPackageIoUtil.builder().osDistEnv(UBUNTU_1604)
                                                          .isOsRelease(true).osReleaseVersionId("16.04").build();
    IDownloadConfig downloadConfig = new Mongo42xDownloadConfigBuilder(ioUtil).defaultsForCommand(command)
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
  public MongoClient mongoClient() throws IOException {
    ServerAddress serverAddress = getServerAddress();
    MongoClientSettings settings = MongoClientSettings.builder()
                                                      .clusterSettings(ClusterSettings.builder()
                                                                                      .hosts(singletonList(serverAddress))
                                                                                      .requiredClusterType(STANDALONE)
                                                                                      .build()).build();
    return MongoClients.create(settings);
  }

  private ServerAddress getServerAddress() throws UnknownHostException {
    return new ServerAddress(mongodProcess.getConfig().net().getServerAddress(),
                             mongodProcess.getConfig().net().getPort());
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

}
