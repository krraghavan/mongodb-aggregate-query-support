package com.github.krr.mongodb.aggregate.support.deserializers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class BsonDocumentObjectMapperTest {

  @DataProvider
  public static Object[][] dateFromBsonDateFixture() {
    long now = Instant.now().toEpochMilli();
    String json = "{\"date\":".concat("{\"$date\":").concat(String.valueOf(now)).concat("}}");
    return new Object[][] {
        new Object[] {json, now},
    };
  }

  @DataProvider
  public static Object[][] longFromBsonDateFixture() throws ParseException {
    long now = Instant.now().toEpochMilli();
    String json = "{\"longValue\":".concat("{\"$date\":").concat(String.valueOf(now)).concat("}}");
    String json1 = "{\"longValue\":".concat("{\"$date\":").concat("\"3680-02-27T07:45:09Z\"").concat("}}");
    TemporalAccessor time1 = DateTimeFormatter.ISO_DATE_TIME.parse("3680-02-27T07:45:09Z");
    return new Object[][] {
        new Object[] {json, now},
        new Object[] {json1, Instant.from(time1).toEpochMilli()}
    };
  }

  @DataProvider
  public static Object[][] longFromBsonStringFixture() {
    long now = Instant.now().toEpochMilli();
    String json = "{\"longValue\":".concat("\"").concat(String.valueOf(now)).concat("\"}");
    return new Object[][] {
        new Object[] {json, now}
    };
  }

  @Data
  static class DateFromString {
    private Date date;
  }

  @Data
  static class LongFromLong {
    private Long longValue;
  }

  @Test(dataProvider = "dateFromBsonDateFixture")
  public void mustResolveDateToDate(String json, long time) throws JsonProcessingException {

    BsonDocumentObjectMapper objectMapper = new BsonDocumentObjectMapper();
    DateFromString dateFromString = objectMapper.readValue(json, DateFromString.class);
    Assert.assertNotNull(dateFromString);
    Assert.assertEquals(dateFromString.getDate().getTime(), time);
  }

  @Test(dataProvider = "longFromBsonDateFixture")
  public void mustResolveBsonDateToLong(String json, long time) throws JsonProcessingException {
    BsonDocumentObjectMapper objectMapper = new BsonDocumentObjectMapper();
    LongFromLong longFromDate = objectMapper.readValue(json, LongFromLong.class);
    Assert.assertNotNull(longFromDate);
    Assert.assertEquals((long)longFromDate.getLongValue(), time);
  }

  @Test(dataProvider = "longFromBsonStringFixture")
  public void mustResolveStringLongToLong(String json, long time) throws JsonProcessingException {
    BsonDocumentObjectMapper objectMapper = new BsonDocumentObjectMapper();
    LongFromLong longFromLong = objectMapper.readValue(json, LongFromLong.class);
    Assert.assertNotNull(longFromLong);
    Assert.assertEquals((long)longFromLong.getLongValue(), time);
  }

  @Test(expectedExceptions = IOException.class)
  public void mustThrowExceptionIfValueIsNotAValidLong() throws JsonProcessingException {
    BsonDocumentObjectMapper objectMapper = new BsonDocumentObjectMapper();
    String json = "{date:\"".concat(RandomStringUtils.randomAlphabetic(10)).concat("\"}");
    objectMapper.readValue(json, LongFromLong.class);
  }

  @Test
  public void mustDeserializeToSubclassWithConstructorParam() throws JsonProcessingException {
    BsonDocumentObjectMapper objectMapper = new BsonDocumentObjectMapper();
    String randomType = RandomStringUtils.randomAlphabetic(10);
    String json = "{\"type\":\"".concat(randomType).concat("\"}");
    ConcreteBean expectedConcreteBean = new ConcreteBean(randomType);
    ConcreteBean actualConcreteBean = objectMapper.readValue(json, ConcreteBean.class);
    Assert.assertNotNull(actualConcreteBean);
    Assert.assertEquals(actualConcreteBean.getType(), expectedConcreteBean.getType());
  }

  static abstract class BaseBean {

    @Getter
    protected final String type;

    protected BaseBean(String type) {
      this.type = type;
    }
  }

  @EqualsAndHashCode(callSuper = true)
  static class ConcreteBean extends BaseBean {

    @JsonCreator
    public ConcreteBean(@JsonProperty("type") String type) {
      super(type);
    }
  }
}