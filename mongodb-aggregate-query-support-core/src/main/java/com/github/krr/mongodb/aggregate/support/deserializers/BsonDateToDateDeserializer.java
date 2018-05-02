package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by rkolliva
 * 4/30/18.
 */

public class BsonDateToDateDeserializer extends GenericMongoExtendedJsonDeserializer<Date> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BsonDateToDateDeserializer.class);

  private static final String NODE_KEY = "$date";

  public BsonDateToDateDeserializer() {
    super(Date.class, NODE_KEY);
  }

  @Override
  protected Date doDeserialize(JsonNode s) {
    return new Date(Long.parseLong(s.asText()));
  }
}
