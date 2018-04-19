/*
 *  Copyright (c) 2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 */
package com.cisco.mongodb.aggregate.support.test.beans;

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
