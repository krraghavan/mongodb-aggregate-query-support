package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Skip;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * camejavi 3/15/17
 */

public interface ReactiveSkipRepository extends ReactiveMongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class)
  @Skip(query = "3", order = 0)
  Flux<Score> getScoresAfterSkip();

}