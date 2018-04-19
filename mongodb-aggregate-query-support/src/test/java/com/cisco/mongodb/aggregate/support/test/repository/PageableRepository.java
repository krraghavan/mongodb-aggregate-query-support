package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.annotation.Out;
import com.cisco.mongodb.aggregate.support.annotation.Sort;
import com.cisco.mongodb.aggregate.support.annotation.v2.*;
import com.cisco.mongodb.aggregate.support.test.beans.Score;
import org.springframework.data.domain.Page;
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

  @Aggregate(inputType = Score.class, outputBeanType = Score.class,
             sort = {
                 @Sort(query = "{ score : 1 }", order = 0)
             })
  Page<Score> getPageableScoresWithInvalidReturnType(Pageable pageable);

  @Aggregate2(inputType = Score.class, outputBeanType = Score.class)
  @Sort2(query = "{ score : 1 }", order = 0)
  Page<Score> getPageableScores2(Pageable pageable);

  @Aggregate2(inputType = Score.class, outputBeanType = Score.class)
  @Facet2(pipelines = {
    @FacetPipeline(name="documents", stages = {
        @FacetPipelineStage(stageType = Match2.class, query = "{'score' : {'$lt' : 100}}")
    })
  }, order = 0)
  @Unwind2(query = "'$documents'", order = 1)
  @ReplaceRoot2(query = "{" +
                        "   \"newRoot\" : \"$documents\"" +
                        "}", order = 2)
  @Sort2(query = "{ score : 1 }", order = 3)
  Page<Score> getPageableWithFacet(Pageable pageable);

}