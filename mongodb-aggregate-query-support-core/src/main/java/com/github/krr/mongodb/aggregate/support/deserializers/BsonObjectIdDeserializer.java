package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.JsonNode;
import org.bson.types.ObjectId;

/**
 * Created by rkolliva
 * 4/30/18.
 */


public class BsonObjectIdDeserializer extends GenericMongoExtendedJsonDeserializer<ObjectId> {

  private static final String NODE_KEY = "$oid";

  protected BsonObjectIdDeserializer() {
    super(ObjectId.class, NODE_KEY);
  }

  @Override
  protected ObjectId doDeserialize(JsonNode s) {
    return new ObjectId(s.textValue());
  }
}
