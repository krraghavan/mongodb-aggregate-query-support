// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans;

import org.springframework.data.annotation.Id;

import java.util.Objects;

/**
 * Created by rkolliva on 10/10/2015.
 */

public class TestAggregateAnnotationBean extends AbstractTestAggregateBean {

  private String randomAttribute;

  public TestAggregateAnnotationBean() {
  }

  @Id
  private String oid;

  public TestAggregateAnnotationBean(String randomAttribute) {
    this.randomAttribute = randomAttribute;
  }

  public String getRandomAttribute() {
    return randomAttribute;
  }

  public void setRandomAttribute(String randomAttribute) {
    this.randomAttribute = randomAttribute;
  }

  public String getOid() {
    return oid;
  }

  public void setOid(String oid) {
    this.oid = oid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestAggregateAnnotationBean that = (TestAggregateAnnotationBean) o;
    return Objects.equals(randomAttribute, that.randomAttribute) &&
           Objects.equals(oid, that.oid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(randomAttribute, oid);
  }
}
