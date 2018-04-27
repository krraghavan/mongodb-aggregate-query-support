package com.github.krr.mongodb.aggregate.support.bsoncodecs;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.ObjectIdCodec;
import org.bson.codecs.StringCodec;
import org.bson.types.ObjectId;

/**
 * If the object id is stored as a string in the beans, BSON decoding
 * fails because it expects the property type to be ObjectId.  This
 * codec and the corresponding provider provides a fix for that issue.
 *
 * Created by rkolliva
 * 4/19/18.
 */
public class ObjectIdAsStringCodec extends StringCodec {

  private static final ObjectIdCodec objectIdCodec = new ObjectIdCodec();

  @Override
  public String decode(BsonReader reader, DecoderContext decoderContext) {
    if(reader.getCurrentBsonType() == BsonType.OBJECT_ID) {
      ObjectId objectId = objectIdCodec.decode(reader, decoderContext);
      return objectId.toString();
    }
    return super.decode(reader, decoderContext);
  }
}
