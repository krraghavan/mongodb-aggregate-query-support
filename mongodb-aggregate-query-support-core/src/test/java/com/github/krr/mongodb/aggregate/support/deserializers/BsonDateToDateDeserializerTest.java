package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class BsonDateToDateDeserializerTest {

  private static final ObjectMapper MAPPER = new BsonDocumentObjectMapper();

  @Data
  @NoArgsConstructor
  public static class TestBean {

    private Date someDate;
  }

  @DataProvider
  public Object [][] dateFixtures() throws ParseException {
    long epochMilli = Instant.now().toEpochMilli();
    Date expectedDate = new Date(epochMilli);
    // Quoted "Z" to indicate UTC, no timezone offset
    DateFormat dfMsec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS'Z'");
    String nowAsISOMsec = dfMsec.format(expectedDate);
    DateFormat dfNoMsec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    String nowAsISONoMsec = dfNoMsec.format(expectedDate);
    Date expectedDateNoMsec = dfNoMsec.parse(nowAsISONoMsec);
    return new Object[][] {
      // milliseconds
      new Object[] {"{\"someDate\":" + String.format("{\"$date\":{\"$numberLong\":\"%s\"}}", epochMilli) + "}",
                    expectedDate},
      // ISO String, msec
      new Object[] {"{\"someDate\":{\"$date\":\"" + nowAsISOMsec + "\"}}", expectedDate},
      // ISO String, no msec
      new Object[] {"{\"someDate\":{\"$date\":\"" + nowAsISONoMsec + "\"}}", expectedDateNoMsec},
    };
  }

  @Test(dataProvider = "dateFixtures")
  public void mustDeserializeDatesInDifferentFormats(String json, Date expectedDate) throws JsonProcessingException {
    TestBean testBean = MAPPER.readValue(json, TestBean.class);
    Assert.assertEquals(testBean.someDate, expectedDate);
  }

}