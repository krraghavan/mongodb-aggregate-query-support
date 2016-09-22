// Copyright (c) 2015-2016 by Cisco Systems, Inc.
/*
 *  Copyright (c) 2016 by Cisco Systems, Inc.
 *
 */

package com.cisco.spring.data.mongodb.test.beans;

import org.jongo.marshall.jackson.oid.MongoId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.List;

/**
 * @author rkolliva.
 */
public class TestPrimaryKeyBean extends AbstractTestAggregateBean {

  @Id
  @MongoId
  private String randomPrimaryKey;

  @Transient
  private List<TestForeignKeyBean> foreignKeyBeanList;

  public String getRandomPrimaryKey() {
    return randomPrimaryKey;
  }

  public void setRandomPrimaryKey(String randomPrimaryKey) {
    this.randomPrimaryKey = randomPrimaryKey;
  }

  public List<TestForeignKeyBean> getForeignKeyBeanList() {
    return foreignKeyBeanList;
  }

  public void setForeignKeyBeanList(List<TestForeignKeyBean> foreignKeyBeanList) {
    this.foreignKeyBeanList = foreignKeyBeanList;
  }
}
