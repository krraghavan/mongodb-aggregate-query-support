package com.github.krr.mongodb.embeddedmongo.config;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mongo42xDownloadConfigBuilder extends DownloadConfigBuilder {

  public DownloadConfigBuilder packageResolverForCommand(Command command) {
    packageResolver(new Mongo42xPaths(command));
    return this;
  }

}
