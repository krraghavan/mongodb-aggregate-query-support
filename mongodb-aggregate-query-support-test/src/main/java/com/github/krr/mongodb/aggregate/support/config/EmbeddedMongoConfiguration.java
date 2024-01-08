package com.github.krr.mongodb.aggregate.support.config;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.embed.process.io.directories.PersistentDir;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.LocalDownloadCache;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

@Configuration
public class EmbeddedMongoConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedMongoConfiguration.class);

  private RunningMongodProcess mongodProcess;

  private int mongoDbPort;

  @PreDestroy
  public void clean() {
    mongodProcess.stop();
  }

  public EmbeddedMongoConfiguration() {
  }

  @PostConstruct
  public void startMongo() throws IOException {
    mongoDbPort = de.flapdoodle.net.Net.freeServerPort();
    Version runningVersion = version(System.getProperty("mongoVersion"));
    String runningMongoVersion = runningVersion.numericVersion().asString();
    System.out.println("Tests will use Mongo version:" + runningMongoVersion);
    LOGGER.info("Starting MongoDb process (version:{}) on port {}", runningMongoVersion, mongoDbPort);
    ImmutableMongod.Builder mongodBuilder = Mongod.builder();
    // to support parallel executions, we may have multiple downloads in parallel.  In order to
    // avoid clobbering in progress downloads we download to a diff directory each time.  Do this
    // only if not on Mac so that local builds don't download unnecessarily.
    Path basedir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()).toPath();
    mongodBuilder.downloadCache(Derive.given(PersistentDir.class)
                                      .state(DownloadCache.class)
                                      .deriveBy(storeBaseDir -> new LocalDownloadCache(basedir))
                                      .withTransitionLabel("downloadCache"));
    Mongod mongod = mongodBuilder.net(Start.to(Net.class).initializedWith(Net.defaults().withPort(mongoDbPort)))
                                 .processOutput(Start.to(ProcessOutput.class)
                                                     .providedBy(Try.<ProcessOutput,
                                                                        IOException>supplier(() ->
                                                                                                 ProcessOutput.builder()
                                                                                                              .output(
                                                                                                                  Processors.named(
                                                                                                                      "[mongod>]",
                                                                                                                      new FileStreamProcessor(
                                                                                                                          new File(
                                                                                                                              "target/mongod.log"))))
                                                                                                              .error(
                                                                                                                  new FileStreamProcessor(
                                                                                                                      new File(
                                                                                                                          "target/mongod-error.log")))
                                                                                                              .commands(
                                                                                                                  Processors.namedConsole(
                                                                                                                      "[console>]"))
                                                                                                              .build())
                                                                    .mapToUncheckedException(
                                                                        RuntimeException::new))
                                                     .withTransitionLabel("create named console"))
                                 .build();
    mongodProcess = mongod.start(runningVersion).current();
  }

  private Version version(String mongoVersion) {
    Version[] versions = Version.values();
    // fragile - counting on convention in this ennm
    Version latest = versions[versions.length - 2];
    if (mongoVersion == null) {
      return getLatestMongoVersion("null", latest);
    }
    return Arrays.stream(Version.values()).filter(version -> version.numericVersion().asString().equals(mongoVersion))
                 .findFirst()
                 .orElseGet(() -> getLatestMongoVersion(mongoVersion, latest));
  }

  private static Version getLatestMongoVersion(String mongoVersion, Version latest) {
    LOGGER.warn("Mongo version {} not supported.  Running tests with LATEST version {}", mongoVersion,
                latest.numericVersion().asString());
    return latest;
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
        LOGGER.error("EmbeddedMongoConfiguration::onProcessed:", e);
      }
    }

    @Override
    public void onProcessed() {
      try {
        outputStream.close();
      }
      catch (IOException e) {
        LOGGER.error("EmbeddedMongoConfiguration::onProcessed:", e);
      }
    }
  }
}
