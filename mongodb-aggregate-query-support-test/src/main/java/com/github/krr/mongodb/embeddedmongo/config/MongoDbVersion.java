package com.github.krr.mongodb.embeddedmongo.config;

import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.EnumSet;

@Slf4j
public class MongoDbVersion implements IFeatureAwareVersion {
  private final String specificVersion;
  private final EnumSet<Feature> features;
  private static final Feature[] DEFAULT_42x_FEATURES = (Feature[]) Arrays.asList(Feature.SYNC_DELAY,
                                                                                  Feature.STORAGE_ENGINE,
                                                                                  Feature.ONLY_64BIT,
                                                                                  Feature.NO_CHUNKSIZE_ARG,
                                                                                  Feature.MONGOS_CONFIGDB_SET_STYLE,
                                                                                  Feature.NO_HTTP_INTERFACE_ARG,
                                                                                  Feature.ONLY_WINDOWS_2008_SERVER,
                                                                                  Feature.NO_SOLARIS_SUPPORT,
                                                                                  Feature.NO_BIND_IP_TO_LOCALHOST)
                                                                          .toArray();

  public MongoDbVersion(String vName, Feature... features) {
    this.specificVersion = vName;
    this.features = Feature.asSet(features);
  }

  public MongoDbVersion(String vName) {
    this(vName, DEFAULT_42x_FEATURES);
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
