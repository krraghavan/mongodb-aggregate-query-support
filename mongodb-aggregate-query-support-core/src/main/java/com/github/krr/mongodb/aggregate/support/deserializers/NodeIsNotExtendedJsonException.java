package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Created by rkolliva
 * 4/30/18.
 */


@SuppressWarnings("WeakerAccess")
public class NodeIsNotExtendedJsonException extends IOException {

  private final JsonNode jsonNode;

  public NodeIsNotExtendedJsonException(JsonNode jsonNode) {
    super();
    this.jsonNode = jsonNode;
  }

  public JsonNode getJsonNode() {
    return jsonNode;
  }
}
