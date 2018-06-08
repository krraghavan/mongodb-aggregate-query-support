package com.github.krr.mongodb.aggregate.support.repository.reactive;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Match;
import com.github.krr.mongodb.aggregate.support.beans.Person;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

import java.util.List;

@NoRepositoryBean
public interface ReactivePersonRepository<T extends Person> extends ReactiveMongoRepository<T, String> {

  @Aggregate(inputType = Person.class,
             outputBeanType = Person.class,
             name = "getPersonsBelowForty")
  @Match(order = 0, query = "{'age': { $lt: 40 }}")
  Flux<Person> getPersonsBelowForty();
}
