package com.github.krr.mongodb.aggregate.support.beans;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class Person {

  Person() {
    this.setAge(RandomUtils.nextInt(35, 45));
    this.setName(RandomStringUtils.random(10));
  }

  protected String name;

  protected Integer age;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }
}
