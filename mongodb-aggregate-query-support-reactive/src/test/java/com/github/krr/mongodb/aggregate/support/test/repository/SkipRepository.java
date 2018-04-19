package com.github.krr.mongodb.aggregate.support.test.repository;

import com.github.krr.mongodb.aggregate.support.annotation.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotation.Skip;
import com.github.krr.mongodb.aggregate.support.test.beans.Score;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * camejavi 3/15/17
 */

public interface SkipRepository extends ReactiveMongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class)
  @Skip(query = "3", order = 0)
  Flux<Score> getScoresAfterSkip();

}