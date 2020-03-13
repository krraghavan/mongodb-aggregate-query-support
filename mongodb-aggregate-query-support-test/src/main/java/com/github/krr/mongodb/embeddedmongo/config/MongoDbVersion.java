package com.github.krr.mongodb.embeddedmongo.config;

import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;

@Slf4j
public enum MongoDbVersion implements IFeatureAwareVersion {

  V4_2_4("4.2.4", Feature.SYNC_DELAY, Feature.STORAGE_ENGINE, Feature.ONLY_64BIT, Feature.NO_CHUNKSIZE_ARG,
         Feature.MONGOS_CONFIGDB_SET_STYLE, Feature.NO_HTTP_INTERFACE_ARG,
         Feature.ONLY_WINDOWS_2008_SERVER, Feature.NO_SOLARIS_SUPPORT, Feature.NO_BIND_IP_TO_LOCALHOST)

//  V4_2_2("4.2.2", Feature.SYNC_DELAY, Feature.STORAGE_ENGINE, Feature.ONLY_64BIT, Feature.NO_CHUNKSIZE_ARG, Feature.NO_NOPREALLOC_ARG, Feature.NO_SMALLFILES_ARG, Feature.MONGOS_CONFIGDB_SET_STYLE, Feature.NO_HTTP_INTERFACE_ARG, Feature.ONLY_WINDOWS_2012_SERVER, Feature.NO_SOLARIS_SUPPORT, Feature.NO_GENERIC_LINUX, Feature.NO_BIND_IP_TO_LOCALHOST),

      ;


  private final String specificVersion;

  private EnumSet<Feature> features;

  MongoDbVersion(String vName, Feature... features) {
    this.specificVersion = vName;
    this.features = Feature.asSet(features);
  }

  @Override
  public String asInDownloadPath() {
    return specificVersion;
  }

  @Override
  public boolean enabled(Feature feature) {
    return features.contains(feature);
  }

  @Override
  public EnumSet<Feature> getFeatures() {
    return EnumSet.copyOf(features);
  }

  @Override
  public String toString() {
    return "MongoDbVersion{" + specificVersion + '}';
  }

}
