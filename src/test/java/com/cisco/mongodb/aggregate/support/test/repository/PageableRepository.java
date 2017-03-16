package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.annotation.Skip;
import com.cisco.mongodb.aggregate.support.annotation.Sort;
import com.cisco.mongodb.aggregate.support.test.beans.Score;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * camejavi 3/15/17
 */

public interface PageableRepository extends MongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, outputBeanType = Score.class,
             sort = {
                 @Sort(query = "{ score : 1 }", order = 0)
             })
  List<Score> getPageableScores(Pageable pageable);

}