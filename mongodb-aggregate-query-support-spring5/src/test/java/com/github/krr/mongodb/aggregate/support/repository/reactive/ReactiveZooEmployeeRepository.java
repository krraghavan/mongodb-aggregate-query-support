package com.github.krr.mongodb.aggregate.support.repository.reactive;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.Employee;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveTestMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveZooEmployeeRepository extends ReactiveTestMongoRepository<Employee, String> {

  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, salaries: { $sum: \"$salary\" } } }",
         order = 0)
  @Merge(query = "{ into: ?1, " +
                 "on: \"_id\",  whenMatched: \"replace\", whenNotMatched: \"insert\" } }")
  Mono<Void> createBudgetsCollection(@CollectionName String coll, String budgetCollection);

  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, salaries: { $sum: \"$salary\" } } }",
         order = 0)
  @Merge(query = "{ into: ?1, " +
                 "on: \"_id\",  whenMatched: \"replace\", whenNotMatched: \"insert\" } }")
  Mono<Void> createBudgetsIntoCollection(@CollectionName String employeeCollection, String budgetCollection);

  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Match(query = "{ fiscalYear:  { $gte : 2019 } }", order = 0)
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, salaries: { $sum: \"$salary\" } } }",
      order = 1)
  @Merge(query = "{ into: ?1, on: \"_id\",  whenMatched: \"replace\", whenNotMatched: \"insert\" } }")
  Mono<Void> replaceBudgets(@CollectionName String employeeCollection, String budgetCollection);


  @Aggregate(inputType = Employee.class, outputBeanType = Employee.class, name = "createBudgetCollection")
  @Match(query = "{ \"fiscalYear\" : 2019 }", order = 0)
  @Group(query = "{ _id: { fiscalYear: \"$fiscalYear\", dept: \"$dept\" }, employees: { $push: \"$employee\" } } ",
         order = 1)
  @Project(query = "{ _id: 0, dept: \"$_id.dept\", fiscalYear: \"$_id.fiscalYear\", employees: 1 } ", order = 2)
  @Merge(query = "{ into : {coll: ?1 }, on: [ \"dept\", \"fiscalYear\" ], whenMatched: \"fail\" } ")
  Mono<Void> updateOrgArchiveInsertOnly(@CollectionName String employeeColl, String orgArchiveColl);
}
