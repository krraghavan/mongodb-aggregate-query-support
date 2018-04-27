package com.github.krr.mongodb.aggregate.support.bsoncodecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rkolliva
 * 4/19/18.
 */
@SuppressWarnings("unchecked")
public class ObjectIdAsStringCodecProvider implements CodecProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectIdAsStringCodecProvider.class);

  @Override
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
    if(clazz == String.class) {
      return (Codec<T>) new ObjectIdAsStringCodec();
    }
    return null;
  }
}
