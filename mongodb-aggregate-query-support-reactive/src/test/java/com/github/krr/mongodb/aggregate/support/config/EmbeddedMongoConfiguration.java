package com.github.krr.mongodb.aggregate.support.config;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
                          .processOutput(
                              Start.to(ProcessOutput.class)
                                   .providedBy(Try.<ProcessOutput,
                                              IOException>supplier(() ->
                                                                       ProcessOutput.builder()
                                                                                    .output(
                                                                                        Processors.named(
                                                                                            "[mongod>]",
                                                                                            new FileStreamProcessor(
                                                                                                new File("mongod.log"))))
                                                                                    .error(
                                                                                        new FileStreamProcessor(
                                                                                            new File("mongod-error.log")))
                                                                                    .commands(
                                                                                        Processors.namedConsole(
                                                                                            "[console>]"))
                                                                                    .build())
                                          .mapToUncheckedException(RuntimeException::new))
                                   .withTransitionLabel("create named console"))
                          .build();
    mongodProcess = mongod.start(Version.V7_0_4).current();
  }

  @Bean
  public int mongoDbPort() {
    return mongoDbPort;
  }

  private static class FileStreamProcessor implements StreamProcessor {

    private final FileOutputStream outputStream;

    public FileStreamProcessor(File file) throws FileNotFoundException {
      outputStream = new FileOutputStream(file);
    }

    @Override
    public void process(String block) {
      try {
        outputStream.write(block.getBytes());
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onProcessed() {
      try {
        outputStream.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

}
