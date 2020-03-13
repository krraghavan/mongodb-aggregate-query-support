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
package com.github.krr.mongodb.aggregate.support.tests.nonreactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.mongodb.aggregate.support.beans.Budget;
import com.github.krr.mongodb.aggregate.support.beans.Employee;
import com.github.krr.mongodb.aggregate.support.beans.OrgArchiveEntry;
import com.github.krr.mongodb.aggregate.support.beans.Salary;
import com.github.krr.mongodb.aggregate.support.config.NonReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.nonreactive.ZooEmployeeRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Created by camejavi on 6/9/2016.
 */
@SuppressWarnings({"Duplicates", "FieldCanBeLocal", "unused"})
@ContextConfiguration(classes = {NonReactiveAggregateTestConfiguration.class})
public class AggregateMergeTest extends AbstractTestNGSpringContextTests {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final String EMPLOYEE_DOCS = "[" +
      "{ \"id\" : 1, \"employee\": \"Ant\", \"dept\": \"A\", \"salary\": 100000, \"fiscalYear\": 2017 }," +
      "{ \"id\" : 2, \"employee\": \"Bee\", \"dept\": \"A\", \"salary\": 120000, \"fiscalYear\": 2017 }," +
      "{ \"id\" : 3, \"employee\": \"Cat\", \"dept\": \"Z\", \"salary\": 115000, \"fiscalYear\": 2017 }," +
      "{ \"id\" : 4, \"employee\": \"Ant\", \"dept\": \"A\", \"salary\": 115000, \"fiscalYear\": 2018 }," +
      "{ \"id\" : 5, \"employee\": \"Bee\", \"dept\": \"Z\", \"salary\": 145000, \"fiscalYear\": 2018 }," +
      "{ \"id\" : 6, \"employee\": \"Cat\", \"dept\": \"Z\", \"salary\": 135000, \"fiscalYear\": 2018 }," +
      "{ \"id\" : 7, \"employee\": \"Gecko\", \"dept\": \"A\", \"salary\": 100000, \"fiscalYear\": 2018 }," +
      "{ \"id\" : 8, \"employee\": \"Ant\", \"dept\": \"A\", \"salary\": 125000, \"fiscalYear\": 2019 }," +
      "{ \"id\" : 9, \"employee\": \"Bee\", \"dept\": \"Z\", \"salary\": 160000, \"fiscalYear\": 2019 }," +
      "{ \"id\" : 10, \"employee\": \"Cat\", \"dept\": \"Z\", \"salary\": 150000, \"fiscalYear\": 2019 }" +
   "]" 
  ;

  private final String ORG_ARCHIVE_DOCS = "[" +
      "{ \"id\" : \"5cd8c68261baa09e9f3622be\", \"employees\" : [ \"Ant\", \"Gecko\" ], \"dept\" : \"A\", \"fiscalYear\" : 2018}," +
      "{ \"id\" : \"5cd8c68261baa09e9f3622bf\", \"employees\" : [ \"Ant\", \"Bee\" ], \"dept\" : \"A\", \"fiscalYear\" : 2017}," +
      "{ \"id\" : \"5cd8c68261baa09e9f3622c0\", \"employees\" : [ \"Bee\", \"Cat\" ], \"dept\" : \"Z\", \"fiscalYear\" : 2018 }," +
      "{ \"id\" : \"5cd8c68261baa09e9f3622c1\", \"employees\" : [ \"Cat\" ], \"dept\" : \"Z\", \"fiscalYear\" : 2017 }" +
  "]";

  private final String ADDITIONAL_EMPLOYEE_DOCS = "[" +
    "{ \"id\" : 11,  \"employee\": \"Wren\", \"dept\": \"Z\", \"salary\": 100000, \"fiscalYear\": 2019 }," +
    "{ \"id\" : 12,  \"employee\": \"Zebra\", \"dept\": \"A\", \"salary\": 150000, \"fiscalYear\": 2019 }," +
    "{ \"id\" : 13,  \"employee\": \"headcount1\", \"dept\": \"Z\", \"salary\": 120000, \"fiscalYear\": 2020 }," +
    "{ \"id\" : 14,  \"employee\": \"headcount2\", \"dept\": \"Z\", \"salary\": 120000, \"fiscalYear\": 2020 }" +
  "]"
  ;

  private final String EXPECTED_BUDGETS_DOCS = "[" +
    "{ \"id\" : { \"fiscalYear\" : 2017, \"dept\" : \"A\" }, \"salaries\" : 220000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2017, \"dept\" : \"Z\" }, \"salaries\" : 115000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2018, \"dept\" : \"A\" }, \"salaries\" : 215000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2018, \"dept\" : \"Z\" }, \"salaries\" : 280000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2019, \"dept\" : \"A\" }, \"salaries\" : 125000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2019, \"dept\" : \"Z\" }, \"salaries\" : 310000 }" +
  "]";

  private final String MATERIALIZED_VIEW_BUDGETS_DOCS = "[" +
    "{ \"id\" : { \"fiscalYear\" : 2017, \"dept\" : \"A\" }, \"salaries\" : 220000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2017, \"dept\" : \"Z\" }, \"salaries\" : 115000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2018, \"dept\" : \"A\" }, \"salaries\" : 215000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2018, \"dept\" : \"Z\" }, \"salaries\" : 280000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2019, \"dept\" : \"A\" }, \"salaries\" : 275000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2019, \"dept\" : \"Z\" }, \"salaries\" : 410000 }," +
    "{ \"id\" : { \"fiscalYear\" : 2020, \"dept\" : \"Z\" }, \"salaries\" : 240000 }" +
  "]";

  private final String SALARIES_DOCUMENTS = "[" +
    "{ \"id\" : 1, \"employee\" : \"Ant\", \"dept\" : \"A\", \"salary\" : 100000, \"fiscalYear\" : 2017 }," +
    "{ \"id\" : 2, \"employee\" : \"Bee\", \"dept\" : \"A\", \"salary\" : 120000, \"fiscalYear\" : 2017 }," +
    "{ \"id\" : 3, \"employee\" : \"Cat\", \"dept\" : \"Z\", \"salary\" : 115000, \"fiscalYear\" : 2017 }," +
    "{ \"id\" : 4, \"employee\" : \"Ant\", \"dept\" : \"A\", \"salary\" : 115000, \"fiscalYear\" : 2018 }," +
    "{ \"id\" : 5, \"employee\" : \"Bee\", \"dept\" : \"Z\", \"salary\" : 145000, \"fiscalYear\" : 2018 }," +
    "{ \"id\" : 6, \"employee\" : \"Cat\", \"dept\" : \"Z\", \"salary\" : 135000, \"fiscalYear\" : 2018 }," +
    "{ \"id\" : 7, \"employee\" : \"Gecko\", \"dept\" : \"A\", \"salary\" : 100000, \"fiscalYear\" : 2018 }," +
    "{ \"id\" : 8, \"employee\" : \"Ant\", \"dept\" : \"A\", \"salary\" : 125000, \"fiscalYear\" : 2019 }," +
    "{ \"id\" : 9, \"employee\" : \"Bee\", \"dept\" : \"Z\", \"salary\" : 160000, \"fiscalYear\" : 2019 }," +
    "{ \"id\" : 10, \"employee\" : \"Cat\", \"dept\" : \"Z\", \"salary\" : 150000, \"fiscalYear\" : 2019 }," +
    "{ \"id\" : 11, \"employee\" : \"Wren\", \"dept\" : \"Z\", \"salary\" : 100000, \"fiscalYear\" : 2019 }," +
    "{ \"id\" : 12, \"employee\" : \"Zebra\", \"dept\" : \"A\", \"salary\" : 150000, \"fiscalYear\" : 2019 }," +
    "{ \"id\" : 13, \"employee\" : \"headcount1\", \"dept\" : \"Z\", \"salary\" : 120000, \"fiscalYear\" : 2020 }," +
    "{ \"id\" : 14, \"employee\" : \"headcount2\", \"dept\" : \"Z\", \"salary\" : 120000, \"fiscalYear\" : 2020 }" +
  "]";

  @Autowired
  private ZooEmployeeRepository zooEmployeeRepository;

  @Autowired
  private MongoOperations mongoTemplate;


  private List<Employee> addEmployeeDocuments(String collectionName, String coll) throws JsonProcessingException {
    TypeReference<List<Employee>> employeeType = new TypeReference<List<Employee>>() {};
    List<Employee> employees = OBJECT_MAPPER.readValue(coll, employeeType);
    mongoTemplate.insert(employees, collectionName);
    return employees;
  }

  @SuppressWarnings("SameParameterValue")
  private List<OrgArchiveEntry> addOrgArchiveEntries(String collectionName, String coll) throws JsonProcessingException {
    TypeReference<List<OrgArchiveEntry>> employeeType = new TypeReference<List<OrgArchiveEntry>>() {};
    List<OrgArchiveEntry> employees = OBJECT_MAPPER.readValue(coll, employeeType);
    mongoTemplate.insert(employees, collectionName);
    return employees;
  }

  private List<Salary> addSalaries(String collectionName, String coll) throws JsonProcessingException {
    TypeReference<List<Salary>> salaryType = new TypeReference<List<Salary>>() {};
    List<Salary> salaries = OBJECT_MAPPER.readValue(coll, salaryType);
    mongoTemplate.insert(salaries, collectionName);
    return salaries;
  }

  @Test
  public void mergeMustMergeObjectsIntoOutRepository() throws JsonProcessingException {
    String employeeCollection = RandomStringUtils.randomAlphabetic(10);
    String budgetCollection = RandomStringUtils.randomAlphabetic(10);
    mongoTemplate.insert(addEmployeeDocuments("employee", EMPLOYEE_DOCS), employeeCollection);
    zooEmployeeRepository.createBudgetsCollection(employeeCollection, budgetCollection);
    assertEquals(mongoTemplate.getCollection(budgetCollection).countDocuments(), 6);
    Query query = new Query().with(Sort.by("_id"));
    List<Budget> expectedBudgetDocs = getBudgets(EXPECTED_BUDGETS_DOCS);
    validateBudget(expectedBudgetDocs, mongoTemplate.find(query, Budget.class, budgetCollection));
  }

  private void validateBudget(List<Budget> budgets, List<Budget> expectedBudgetDocs) {
    for(int i = 0; i < expectedBudgetDocs.size(); i++) {
      assertEquals(budgets.get(i), expectedBudgetDocs.get(i));
    }
  }

  @Test
  public void mergeMustCreateOnDemandMaterializedViews() throws JsonProcessingException {
    String budgetsCollection = RandomStringUtils.randomAlphabetic(10);
    String employeeCollection = RandomStringUtils.randomAlphabetic(10);
    List<Employee> originalEmployees = addEmployeeDocuments(employeeCollection, EMPLOYEE_DOCS);
    MongoCollection<Document> employeeColl = mongoTemplate.getCollection(employeeCollection);
    assertEquals(employeeColl.countDocuments(), originalEmployees.size());
    zooEmployeeRepository.createBudgetsIntoCollection(employeeCollection, budgetsCollection);
    List<Budget> expectedBudgetDocs = getBudgets(EXPECTED_BUDGETS_DOCS);
    Query query = new Query().with(Sort.by("_id"));
    validateBudget(expectedBudgetDocs, mongoTemplate.find(query, Budget.class, budgetsCollection));

    // now insert new records into employees
    List<Employee> newEmployees = addEmployeeDocuments(employeeCollection, ADDITIONAL_EMPLOYEE_DOCS);
    assertEquals(employeeColl.countDocuments(), originalEmployees.size() + newEmployees.size());
    zooEmployeeRepository.replaceBudgets(employeeCollection, budgetsCollection);
    List<Budget> expectedBudgetDocs1 = getBudgets(MATERIALIZED_VIEW_BUDGETS_DOCS);
    validateBudget(expectedBudgetDocs1, mongoTemplate.find(query, Budget.class, budgetsCollection));
  }

  @Test
  public void mustNotOverwriteExistingDocuments() throws JsonProcessingException {
    String employeeCollection = RandomStringUtils.randomAlphabetic(10);
    String orgArchiveColl = RandomStringUtils.randomAlphabetic(10);
    List<Employee> originalEmployees = addEmployeeDocuments(employeeCollection, EMPLOYEE_DOCS);
    MongoCollection<Document> employeeColl = mongoTemplate.getCollection(employeeCollection);
    assertEquals(employeeColl.countDocuments(), originalEmployees.size());
    List<OrgArchiveEntry> orgArchiveEntries = addOrgArchiveEntries(orgArchiveColl, ORG_ARCHIVE_DOCS);
    MongoCollection<Document> orgArchiveCollection = mongoTemplate.getCollection(orgArchiveColl);
    assertEquals(orgArchiveCollection.countDocuments(), orgArchiveEntries.size());
    orgArchiveCollection.createIndex(BsonDocument.parse("{ \"fiscalYear\": 1, \"dept\": 1 }"), new IndexOptions().unique(true));
    zooEmployeeRepository.updateOrgArchiveInsertOnly(employeeCollection, orgArchiveColl);
    assertEquals(orgArchiveCollection.countDocuments(), orgArchiveEntries.size() + 2);
    Query query = new Query(Criteria.where("fiscalYear").is(2019));
    List<OrgArchiveEntry> newArchiveEntries = mongoTemplate.find(query, OrgArchiveEntry.class, orgArchiveColl);
    assertEquals(newArchiveEntries.size(), 2);
  }

  private List<Budget> getBudgets(String docs) throws JsonProcessingException {
    TypeReference<List<Budget>> budgetsType = new TypeReference<List<Budget>>() {};
    return OBJECT_MAPPER.readValue(docs, budgetsType);
  }

}
