package com.github.krr.mongodb.aggregate.support.repository;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class OrgArchiveEntry {
  @Id
  private String id;

  private List<String> employees;

  private String dept;

  private int fiscalYear;

}
