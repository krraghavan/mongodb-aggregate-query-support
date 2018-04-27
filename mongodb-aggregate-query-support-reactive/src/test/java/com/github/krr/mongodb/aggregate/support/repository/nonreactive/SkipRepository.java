package com.github.krr.mongodb.aggregate.support.repository.nonreactive;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Skip;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * camejavi 3/15/17
 */

public interface SkipRepository extends MongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, outputBeanType = Score.class)
  @Skip(query = "3", order = 0)
  List<Score> getScoresAfterSkip();

}