package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rkolliva
 * 4/30/18.
 */

public class BsonDateToDateDeserializer extends GenericMongoExtendedJsonDeserializer<Date> {

  private static final String NODE_KEY = "$date";

  public BsonDateToDateDeserializer() {
    super(Date.class, NODE_KEY);
  }

  @Override
  protected Date doDeserialize(JsonNode s) {
    JsonNode longFormatNode = s.get("$numberLong");
    if (longFormatNode != null) {
      // date specified in long format
      String longValueAsText = longFormatNode.textValue();
      return new Date(Long.parseLong(longValueAsText));
    }
    else {
      // get the value as text -> if it returns blank throw unsupported exception
      String dateTextValue = s.textValue();
      if (StringUtils.isEmpty(dateTextValue)) {
        throw new IllegalArgumentException("Unrecognized date time format for node " + s.toPrettyString());
      }
      // support ISO format only.  Any other format will result in an exception
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS'Z'");
      try {
        return df.parse(dateTextValue);
      }
      catch (ParseException e) {
        // try with no msecs
        DateFormat dfNoMsec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
          return dfNoMsec.parse(dateTextValue);
        }
        catch (ParseException ex) {
          throw new UnsupportedOperationException("Only ISO date format (with or without msecs) is supported.  " +
                                                  "Could not deserialize " + dateTextValue, e);
        }
      }
    }
  }
}
