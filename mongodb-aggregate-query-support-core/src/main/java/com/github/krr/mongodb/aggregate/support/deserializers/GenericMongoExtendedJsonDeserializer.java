package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by rkolliva
 * 4/30/18.
 */


@SuppressWarnings("WeakerAccess")
public abstract class GenericMongoExtendedJsonDeserializer<T> extends StdDeserializer<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericMongoExtendedJsonDeserializer.class);

  protected final String nodeKey;

  protected GenericMongoExtendedJsonDeserializer(Class<T> clazz, String nodeKey) {
    super(clazz);
    this.nodeKey = nodeKey;
  }

  protected JsonNode getJsonNodeForExtendedBson(JsonParser p) throws IOException {
    LOGGER.trace(">>>> BsonObjectIdToStringDeserializer::getJsonNodeForExtendedBson");
    JsonNode bsonNode = p.getCodec().readTree(p);
    JsonNode jsonNode = bsonNode.get(nodeKey);
    if(jsonNode == null) {
      throw new NodeIsNotExtendedJsonException(bsonNode);
    }
    return jsonNode;
  }

  @Override
  public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

    LOGGER.trace(">>>> GenericMongoExtendedJsonDeserializer::deserialize");
    JsonNode nodeValue = getJsonNodeForExtendedBson(p);
    return doDeserialize(nodeValue);
  }

  protected abstract T doDeserialize(JsonNode s);
}
