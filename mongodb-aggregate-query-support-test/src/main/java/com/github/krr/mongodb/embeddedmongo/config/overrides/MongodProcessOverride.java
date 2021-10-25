package com.github.krr.mongodb.embeddedmongo.config.overrides;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;

import java.io.IOException;

public class MongodProcessOverride extends MongodProcess {

  public MongodProcessOverride(Distribution distribution,
                               MongodConfig config,
                               RuntimeConfig runtimeConfig,
                               MongodExecutable mongodExecutable) throws IOException {
    super(distribution, config, runtimeConfig, mongodExecutable);
  }

  @Override
  protected String successMessage() {
    return "Waiting for connections";
  }
}
