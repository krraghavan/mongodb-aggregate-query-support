// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.beans;

import org.springframework.data.annotation.Id;

/**
 * Created by rkolliva on 10/19/2015.
 * { "_id" : { "month" : 3, "day" : 15, "year" : 2014 }, "totalPrice" : 50, "averageQuantity" : 10, "count" : 1 }
 * { "_id" : { "month" : 4, "day" : 4, "year" : 2014 }, "totalPrice" : 200, "averageQuantity" : 15, "count" : 2 }
 * { "_id" : { "month" : 3, "day" : 1, "year" : 2014 }, "totalPrice" : 40, "averageQuantity" : 1.5, "count" : 2 }
 */
public class TestGroupResultsBean {

  public static class SaleDate {
    private int month;

    private int day;

    private int year;

    public int getMonth() {
      return month;
    }

    public void setMonth(int month) {
      this.month = month;
    }

    public int getDay() {
      return day;
    }

    public void setDay(int day) {
      this.day = day;
    }

    public int getYear() {
      return year;
    }

    public void setYear(int year) {
      this.year = year;
    }
  }

  @Id
  private SaleDate _id;

  private int totalPrice;

  private float averageQuantity;

  private int count;

  public SaleDate getDate() {
    return _id;
  }

  public void setDate(SaleDate date) {
    this._id = date;
  }

  public int getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(int totalPrice) {
    this.totalPrice = totalPrice;
  }

  public float getAverageQuantity() {
    return averageQuantity;
  }

  public void setAverageQuantity(float averageQuantity) {
    this.averageQuantity = averageQuantity;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
