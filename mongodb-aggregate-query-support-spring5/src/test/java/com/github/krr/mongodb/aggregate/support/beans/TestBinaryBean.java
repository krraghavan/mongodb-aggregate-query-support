package com.github.krr.mongodb.aggregate.support.beans;


import com.github.krr.mongodb.aggregate.support.annotations.MongoId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TestBinaryBean {

  @MongoId
  private String id;

  private byte[] md5Hash;

}
