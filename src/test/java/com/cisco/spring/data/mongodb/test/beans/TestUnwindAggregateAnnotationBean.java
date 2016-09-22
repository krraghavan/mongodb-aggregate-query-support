// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkolliva on 10/19/2015.
 */
public class TestUnwindAggregateAnnotationBean extends AbstractTestAggregateBean {

  @Id
  private String oid;

  private List<String> randomListOfStrings = new ArrayList<>();

  private String randomSingleField;

  private int randomIntField;

  public String getOid() {
    return oid;
  }

  public void setOid(String oid) {
    this.oid = oid;
  }

  public List<String> getRandomListOfStrings() {
    return randomListOfStrings;
  }

  public void setRandomListOfStrings(List<String> randomListOfStrings) {
    this.randomListOfStrings = randomListOfStrings;
  }

  public String getRandomSingleField() {
    return randomSingleField;
  }

  public void setRandomSingleField(String randomSingleField) {
    this.randomSingleField = randomSingleField;
  }

  public int getRandomIntField() {
    return randomIntField;
  }

  public void setRandomIntField(int randomIntField) {
    this.randomIntField = randomIntField;
  }
}
