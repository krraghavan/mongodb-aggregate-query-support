package com.github.krr.mongodb.aggregate.support.repository;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Budget {

  @Id
  private BudgetId id;

  private int salaries;

  @SuppressWarnings("WeakerAccess")
  @Data
  public static class BudgetId {
    private int fiscalYear;

    private String dept;
  }

}
