package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by rkolliva
 * 4/30/18.
 */

public class BsonDateToDateDeserializer extends GenericMongoExtendedJsonDeserializer<Date> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BsonDateToDateDeserializer.class);

  public static final String NODE_KEY = "$date";

  public BsonDateToDateDeserializer() {
    super(Date.class, NODE_KEY);
  }

  @Override
  protected Date doDeserialize(JsonNode s) {
    JsonNodeType nodeType = s.getNodeType();
    if(nodeType == JsonNodeType.NUMBER) {
      return new Date(s.asLong());
    }
    else if(nodeType == JsonNodeType.STRING) {
      return Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(s.asText())));
    }
    throw new IllegalArgumentException("unrecognized node type " + nodeType + " with value " + s.asText());
  }
}
