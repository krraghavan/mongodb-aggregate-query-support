package com.github.krr.mongodb.embeddedmongo.config;

import com.github.krr.mongodb.embeddedmongo.config.conditions.NotUseRealMongoCondition;
import com.github.krr.mongodb.embeddedmongo.config.overrides.MacOsPathOverride;
import com.github.krr.mongodb.embeddedmongo.config.overrides.MongodStarterOverride;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.distribution.ImmutableGenericVersion;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.runtime.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@Conditional(NotUseRealMongoCondition.class)
public class MongoTestServerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoTestServerConfiguration.class);

  private static final String LOCALHOST = "localhost";

  private final MongodExecutable mongodExecutable;

  private final MongodProcess mongodProcess;

  public MongoTestServerConfiguration() throws IOException {

    Command command = Command.MongoD;
    DownloadConfig downloadConfig = Defaults.downloadConfigFor(command)
                                            .progressListener(new Slf4jProgressListener(LOGGER))
                                            .packageResolver(new MacOsPathOverride(Command.MongoD))
                                            .build();
    RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(command)
                                          .artifactStore(Defaults.extractedArtifactStoreFor(command)
                                                                 .withDownloadConfig(downloadConfig))
                                          .build();
    final MongodStarterOverride runtime = MongodStarterOverride.getInstance(runtimeConfig);
    mongodExecutable = runtime.prepare(newMongodConfig());
    mongodProcess = mongodExecutable.start();
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

  @Bean
  public MongodProcess mongodProcess() {
    return mongodProcess;
  }

  private MongodConfig newMongodConfig() throws IOException {
    ImmutableMongoCmdOptions.Builder builder = MongoCmdOptions.builder().useSmallFiles(false).useNoPrealloc(false);
    return MongodConfig.builder().version(Versions.withFeatures(ImmutableGenericVersion.of("4.4.10"),
                                                                de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION.getFeatures()))
                       .cmdOptions(builder.build())
                       .net(new Net(LOCALHOST, Network.getFreeServerPort(), Network.localhostIsIPv6())).build();
  }

}
