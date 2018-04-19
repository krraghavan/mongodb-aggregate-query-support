package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.annotation.Skip;
import com.cisco.mongodb.aggregate.support.test.beans.Score;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * camejavi 3/15/17
 */

public interface SkipRepository extends MongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, outputBeanType = Score.class,
             skip = {
                 @Skip(query = "3", order = 0)
             }
  )
  List<Score> getScoresAfterSkip();

}