// Copyright (c) 2015-2016 by Cisco Systems, Inc.
/*
 *  Copyright (c) 2016 by Cisco Systems, Inc.
 *
 */

package com.cisco.spring.data.mongodb.test.beans;

import org.jongo.marshall.jackson.oid.MongoId;
import org.springframework.data.annotation.Id;

/**
 * @author rkolliva.
 */
public class TestForeignKeyBean extends AbstractTestAggregateBean {

  @Id
  @MongoId
  private String randomAttribute;

  private String foreignKey;

  public String getRandomAttribute() {
    return randomAttribute;
  }

  public void setRandomAttribute(String randomAttribute) {
    this.randomAttribute = randomAttribute;
  }

  public String getForeignKey() {
    return foreignKey;
  }

  public void setForeignKey(String foreignKey) {
    this.foreignKey = foreignKey;
  }
}
