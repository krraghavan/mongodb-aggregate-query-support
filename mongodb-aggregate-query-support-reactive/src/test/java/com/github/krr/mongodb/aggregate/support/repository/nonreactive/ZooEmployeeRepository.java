package com.github.krr.mongodb.aggregate.support.repository.nonreactive;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.Employee;

public interface ZooEmployeeRepository extends TestMongoRepository<Employee, String>{

  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, salaries: { $sum: \"$salary\" } } }",
         order = 0)
  @Merge(query = "{ into: ?1, " +
                 "on: \"_id\",  whenMatched: \"replace\", whenNotMatched: \"insert\" } }")
  void createBudgetsCollection(@CollectionName String coll, String budgetCollection);

  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, salaries: { $sum: \"$salary\" } } }",
         order = 0)
  @Merge(query = "{ into: ?1, " +
                 "on: \"_id\",  whenMatched: \"replace\", whenNotMatched: \"insert\" } }")
  void createBudgetsIntoCollection(@CollectionName String employeeCollection, String budgetCollection);

  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Match(query = "{ fiscalYear:  { $gte : 2019 } }", order = 0)
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, salaries: { $sum: \"$salary\" } } }",
      order = 1)
  @Merge(query = "{ into: ?1, on: \"_id\",  whenMatched: \"replace\", whenNotMatched: \"insert\" } }")
  void replaceBudgets(@CollectionName String employeeCollection, String budgetCollection);


  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Match(query = "{ \"fiscalYear\" : 2019 }", order = 0)
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, employees: { $push: \"$employee\" } } ",
         order = 1)
  @Project(query = "{ _id: 0, dept: \"$_id.dept\", fiscalYear: \"$_id.fiscalYear\", employees: 1 } ", order = 2)
  @Merge(query = "{ into : {coll: ?1 }, on: [ \"dept\", \"fiscalYear\" ], whenMatched: \"fail\" } ")
  void updateOrgArchiveInsertOnly(@CollectionName String employeeColl, String orgArchiveColl);
}
