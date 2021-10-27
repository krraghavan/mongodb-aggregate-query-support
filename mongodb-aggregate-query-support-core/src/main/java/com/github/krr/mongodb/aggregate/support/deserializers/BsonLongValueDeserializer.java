package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * This deserializer handles the different ways we can form a long field.
 * NumberLong -> Long
 * TimeStamp -> long
 * String -> long
 */
public class BsonLongValueDeserializer extends StdDeserializer<Long> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BsonNumberLongToLongDeserializer.class);

  private static final BsonNumberLongToLongDeserializer LONG_TO_LONG_DESERIALIZER = new BsonNumberLongToLongDeserializer();

  private static final BsonDateToDateDeserializer DATE_TO_DATE_DESERIALIZER = new BsonDateToDateDeserializer();

  public BsonLongValueDeserializer() {
    super(Long.class);
  }

  /**
   * Longs can be returned even if the json value is represented as an integer
   * instead of a long (which is a JSON object with $numberLong).  Handle both
   * cases here.
   *
   * @param p    - the parser used to read the JSON
   * @param ctxt - the deser. context
   * @return - the long value
   * @throws IOException - if there was a parsing error.
   */
  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    LOGGER.trace(">>>> BsonNumberLongToLongDeserializer::deserialize");
    JsonNode bsonNode = p.getCodec().readTree(p);
    // first try to get the long value as a long
    JsonNode jsonNode = bsonNode.get(BsonNumberLongToLongDeserializer.NODE_KEY);
    if (jsonNode != null) {
      LOGGER.trace("Deserializing BSON NumberLong ");
      return LONG_TO_LONG_DESERIALIZER.doDeserialize(jsonNode);
    }
    // not a long - try time
    jsonNode = bsonNode.get(BsonDateToDateDeserializer.NODE_KEY);
    if (jsonNode != null) {
      LOGGER.trace("Deserializing BSON Date as long");
      Date date = DATE_TO_DATE_DESERIALIZER.doDeserialize(jsonNode);
      return date.getTime();
    }
    // see if this is a number
    if(bsonNode.getNodeType() == JsonNodeType.NUMBER) {
      LOGGER.trace("Deserializing Number as long ");
      return bsonNode.asLong();
    }
    else if(bsonNode.getNodeType() == JsonNodeType.STRING) {
      LOGGER.trace("Deserializing BSON String as long ");
      return Long.parseLong(bsonNode.textValue());
    }
    else if(bsonNode.getNodeType() == JsonNodeType.NULL) {
      LOGGER.trace("Deserializing BSON String as long ");
      return null;
    }
    throw new JsonMappingException(p, "Unsupported type for token " + bsonNode.asText() + ".  Cannot convert to long");
  }
}
