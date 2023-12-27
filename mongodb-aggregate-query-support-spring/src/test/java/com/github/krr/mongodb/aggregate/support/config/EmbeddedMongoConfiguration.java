package com.github.krr.mongodb.aggregate.support.config;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.transitions.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class EmbeddedMongoConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedMongoConfiguration.class);

  private final RunningMongodProcess mongodProcess;

  private final int mongoDbPort;

  @PreDestroy
  public void clean() {
    mongodProcess.stop();
  }

  public EmbeddedMongoConfiguration() throws Exception {

    mongoDbPort = de.flapdoodle.net.Net.freeServerPort();
    LOGGER.info("Starting MongoDb process on port {}", mongoDbPort);

    Mongod mongod = Mongod.builder()
                          .net(Start.to(Net.class).initializedWith(Net.defaults()
                                                                      .withPort(mongoDbPort)))
                          .build();
    mongodProcess = mongod.start(Version.V7_0_4).current();
  }

  @Bean
  public int mongoDbPort() {
    return mongoDbPort;
  }

}
