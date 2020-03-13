package com.github.krr.mongodb.aggregate.support.beans;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Salary {
  @Id
  private String id;

  private String employee;

  private String dept;

  private int salary;

  private int fiscalYear;

}
