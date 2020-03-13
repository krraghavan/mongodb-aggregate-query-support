package com.github.krr.mongodb.aggregate.support.beans;

import com.github.krr.mongodb.aggregate.support.beans.AbstractTestAggregateBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

@EqualsAndHashCode(callSuper = true)
@Data
public class Employee extends AbstractTestAggregateBean {

  @Id
  private String id;

  private String employee;

  private String dept;

  private int salary;

  private int fiscalYear;
}
