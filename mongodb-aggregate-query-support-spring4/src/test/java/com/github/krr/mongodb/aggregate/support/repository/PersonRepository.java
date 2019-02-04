package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Match;
import com.github.krr.mongodb.aggregate.support.beans.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface PersonRepository <T extends Person> extends MongoRepository<T, String> {

  @Aggregate(inputType = Person.class,
             outputBeanType = Person.class,
             name = "getPersonsBelowForty")
  @Match(order = 0, query = "{'age': { $lt: 40 }}")
  List<Person> getPersonsBelowForty();
}

