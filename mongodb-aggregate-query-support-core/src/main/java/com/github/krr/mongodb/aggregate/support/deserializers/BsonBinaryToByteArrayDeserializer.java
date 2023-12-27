package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Base64;

/**
 * Created by rkolliva
 * 4/30/18.
 */

@SuppressWarnings("WeakerAccess")
public class BsonBinaryToByteArrayDeserializer extends GenericMongoExtendedJsonDeserializer<byte[]> {

  public BsonBinaryToByteArrayDeserializer() {
    super(byte[].class, "$binary");
  }

  @Override
  protected byte[] doDeserialize(JsonNode nodeValue) {
    return Base64.getDecoder().decode(nodeValue.get("base64").textValue());
  }
}
