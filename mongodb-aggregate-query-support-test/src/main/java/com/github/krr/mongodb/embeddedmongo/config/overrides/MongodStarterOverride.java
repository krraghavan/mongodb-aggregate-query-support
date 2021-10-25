package com.github.krr.mongodb.embeddedmongo.config.overrides;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Starter;

public class MongodStarterOverride extends Starter<MongodConfig, MongodExecutable, MongodProcess> {

  private MongodStarterOverride(RuntimeConfig config) {
    super(config);
  }

  public static MongodStarterOverride getInstance(RuntimeConfig config) {
    return new MongodStarterOverride(config);
  }

  public static MongodStarterOverride getDefaultInstance() {
    return getInstance(Defaults.runtimeConfigFor(Command.MongoD).build());
  }

  @Override
  protected MongodExecutable newExecutable(MongodConfig mongodConfig,
                                           Distribution distribution,
                                           RuntimeConfig runtime,
                                           ExtractedFileSet files) {
    return new MongodExecutableOverride(distribution, mongodConfig, runtime, files);
  }

}
