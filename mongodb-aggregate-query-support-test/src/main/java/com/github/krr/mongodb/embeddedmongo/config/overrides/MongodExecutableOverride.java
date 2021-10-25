package com.github.krr.mongodb.embeddedmongo.config.overrides;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;

import java.io.IOException;

public class MongodExecutableOverride extends MongodExecutable {

  public MongodExecutableOverride(Distribution distribution,
                                  MongodConfig mongodConfig,
                                  RuntimeConfig runtimeConfig,
                                  ExtractedFileSet files) {
    super(distribution, mongodConfig, runtimeConfig, files);
  }

  @Override
  protected MongodProcess start(Distribution distribution, MongodConfig config, RuntimeConfig runtime)
      throws IOException {
    return new MongodProcessOverride(distribution, config, runtime, this);
  }

}
