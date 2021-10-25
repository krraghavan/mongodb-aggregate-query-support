package com.github.krr.mongodb.aggregate.support.beans;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
@CompoundIndex(unique = true, def = "{'fiscalYear': 1, 'dept': 1}")
public class OrgArchiveEntry {

  @Id
  private String id;

  private List<String> employees;

  private String dept;

  private int fiscalYear;

}
