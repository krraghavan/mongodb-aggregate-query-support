package com.github.krr.mongodb.embeddedmongo.config;

import de.flapdoodle.embed.process.config.store.IDownloadPath;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;

/**
 * Overrides the download path where Linux distributions of MongoDB community server can be found.
 *
 * @author raghavan
 */
public class Mongo42xDownloadPath implements IDownloadPath {

  @Override
  public String getPath(Distribution distribution) {
    if (distribution.getPlatform() == Platform.Windows) {
      return "https://downloads.mongodb.org/";
    }
    else if(distribution.getPlatform() == Platform.Linux) {
      // right now build this for travisci - won't work for other distributions.
      return "http://downloads.mongodb.org/";
    }
    else {
      return "https://fastdl.mongodb.org/";
    }
  }

}

//http://downloads.mongodb.org/linux/mongodb-linux-x86_64-ubuntu1604-4.2.4.tgz