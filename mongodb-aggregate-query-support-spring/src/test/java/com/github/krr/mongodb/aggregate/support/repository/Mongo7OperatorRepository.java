package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Group;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * $percentile and $median are accumulators introduced in MongoDb 7.0.  They live inside the body of
 * a $group stage, which this library passes to the driver as an opaque string, so they need no
 * support of their own.  This repository exists to keep that true.
 */
public interface Mongo7OperatorRepository extends MongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, outputBeanType = Document.class)
  @Group(order = 0, query = "{"
                            + "  \"_id\" : null,"
                            + "  \"medianScore\" : { \"$median\" : "
                            + "      { \"input\" : \"$score\", \"method\" : \"approximate\" } }"
                            + "}")
  Document medianScore();

  @Aggregate(inputType = Score.class, outputBeanType = Document.class)
  @Group(order = 0, query = "{"
                            + "  \"_id\" : null,"
                            + "  \"scorePercentiles\" : { \"$percentile\" : "
                            + "      { \"input\" : \"$score\", \"p\" : [ 0.5, 0.9 ], \"method\" : \"approximate\" } }"
                            + "}")
  Document scorePercentiles();
}
