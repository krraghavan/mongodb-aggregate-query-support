package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class intercepts all String conversions.  It therefore has to handle all
 * the conversion types:
 *
 * $numberLong to String
 *
 * as well as int to string etc.
 * Created by rkolliva
 * 4/30/18.
 */

@SuppressWarnings("WeakerAccess")
public class BsonObjectIdToStringDeserializer extends StdDeserializer<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BsonObjectIdToStringDeserializer.class);

  private static final String NODE_KEY = "$oid";

  private static final JsonNodeDeserializer jsonNodeDeserializer = new JsonNodeDeserializer();

  protected BsonObjectIdToStringDeserializer() {
    super(String.class);
  }

  @Override
  public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    LOGGER.trace(">>>> BsonObjectIdToStringDeserializer::deserialize");
    JsonNode bsonNode = p.getCodec().readTree(p);
    JsonNode nodeValue = bsonNode.get(NODE_KEY);
    if(nodeValue == null) {
      // parse this as a regular String
      if(bsonNode instanceof NumericNode) {
        return bsonNode.asText();
      }
      else if(bsonNode instanceof ObjectNode) {
        return (String) jsonNodeDeserializer.deserializeJsonNode(bsonNode);
      }
      return bsonNode.textValue();
    }
    LOGGER.trace("<<<< BsonObjectIdToStringDeserializer::deserialize");
    return nodeValue.textValue();
  }

}
