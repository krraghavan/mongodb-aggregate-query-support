package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by rkolliva
 * 4/30/18.
 */


public class JsonNodeDeserializer  {

  public Object deserializeJsonNode(JsonNode node) {
    if(node instanceof ObjectNode) {
      ObjectNode objectNode = (ObjectNode)node;
      if(objectNode.has(BsonNumberLongToLongDeserializer.NODE_KEY)) {
        // this is a number long.
        return objectNode.get(BsonNumberLongToLongDeserializer.NODE_KEY).asText();
      }
    }
    return null;
  }
}
