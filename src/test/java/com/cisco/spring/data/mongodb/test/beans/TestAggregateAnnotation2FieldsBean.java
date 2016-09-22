// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans;

import org.springframework.data.annotation.Id;

import java.util.Objects;

/**
 * Created by rkolliva on 10/18/2015.
 */
public class TestAggregateAnnotation2FieldsBean extends AbstractTestAggregateBean {

  private String randomAttribute1;

  private int randomAttribute2;

  @Id
  private String oid;

  public TestAggregateAnnotation2FieldsBean(String randomAttribute1, int randomAttribute2) {
    this.randomAttribute1 = randomAttribute1;
    this.randomAttribute2 = randomAttribute2;
  }

  public TestAggregateAnnotation2FieldsBean() {
  }

  public TestAggregateAnnotation2FieldsBean(String randomAttribute1) {
    this.randomAttribute1 = randomAttribute1;
  }

  public String getRandomAttribute1() {
    return randomAttribute1;
  }

  public void setRandomAttribute1(String randomAttribute1) {
    this.randomAttribute1 = randomAttribute1;
  }

  public String getOid() {
    return oid;
  }

  public void setOid(String oid) {
    this.oid = oid;
  }

  public int getRandomAttribute2() {
    return randomAttribute2;
  }

  public void setRandomAttribute2(int randomAttribute2) {
    this.randomAttribute2 = randomAttribute2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestAggregateAnnotation2FieldsBean that = (TestAggregateAnnotation2FieldsBean) o;
    return Objects.equals(randomAttribute2, that.randomAttribute2) &&
           Objects.equals(randomAttribute1, that.randomAttribute1) &&
           Objects.equals(oid, that.oid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(randomAttribute1, randomAttribute2, oid);
  }
}
