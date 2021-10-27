package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by rkolliva
 * 4/30/18.
 */

public class BsonDocumentObjectMapper extends ObjectMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(BsonDocumentObjectMapper.class);

  public BsonDocumentObjectMapper() {
    super();
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    LOGGER.trace("Registering custom modules");
    SimpleModule module = new SimpleModule();
    module.addDeserializer(byte[].class, new BsonBinaryToByteArrayDeserializer());
    module.addDeserializer(long.class, new BsonLongValueDeserializer());
    module.addDeserializer(Long.class, new BsonLongValueDeserializer());
    module.addDeserializer(String.class, new BsonObjectIdToStringDeserializer());
    module.addDeserializer(ObjectId.class, new BsonObjectIdDeserializer());
    module.addDeserializer(Date.class, new BsonDateToDateDeserializer());
    registerModule(module);
  }
}
