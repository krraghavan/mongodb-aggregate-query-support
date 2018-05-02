package com.github.krr.mongodb.aggregate.support.beans;

import com.github.krr.mongodb.aggregate.support.reactive.annotations.MongoId;
import com.google.common.base.Objects;

public class TestBinaryBean {

  @MongoId
  private String id;

  private byte[] md5Hash;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public byte[] getMd5Hash() {
    return md5Hash;
  }

  public void setMd5Hash(byte[] md5Hash) {
    this.md5Hash = md5Hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestBinaryBean binaryBean = (TestBinaryBean) o;
    return Objects.equal(id, binaryBean.id) &&
           Objects.equal(md5Hash, binaryBean.md5Hash);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, md5Hash);
  }
}
