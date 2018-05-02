package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by rkolliva
 * 4/30/18.
 */


@SuppressWarnings("WeakerAccess")
public class BsonNumberLongToLongDeserializer extends GenericMongoExtendedJsonDeserializer<Long> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BsonNumberLongToLongDeserializer.class);

  public static final String NODE_KEY = "$numberLong";

  public BsonNumberLongToLongDeserializer() {
    super(Long.class, NODE_KEY);
  }

  /**
   * Longs can be returned even if the json value is represented as an integer
   * instead of a long (which is a JSON object with $numberLong).  Handle both
   * cases here.
   *
   * @param p - the parser used to read the JSON
   * @param ctxt - the deser. context
   * @return - the long value
   * @throws IOException - if there was a parsing error.
   */
  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    LOGGER.trace(">>>> BsonNumberLongToLongDeserializer::deserialize");
    try {
      JsonNode nodeValue = getJsonNodeForExtendedBson(p);
      return doDeserialize(nodeValue);
    }
    catch(NodeIsNotExtendedJsonException e) {
      // not formatted as a $numberLong
      LOGGER.trace("<<<< BsonNumberLongToLongDeserializer::deserialize - not a $numberLong");
      return doDeserialize(e.getJsonNode());
    }
  }

  @Override
  protected Long doDeserialize(JsonNode jsonNode) {
    return Long.parseLong(jsonNode.asText());
  }

}
