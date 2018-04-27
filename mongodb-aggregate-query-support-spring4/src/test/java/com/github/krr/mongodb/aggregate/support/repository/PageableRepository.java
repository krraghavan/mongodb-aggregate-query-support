package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.Score;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * camejavi 3/15/17
 */

public interface PageableRepository extends MongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, outputBeanType = Score.class)
  @Sort(query = "{ score : 1 }", order = 0)
  List<Score> getPageableScores(Pageable pageable);

  @Aggregate(inputType = Score.class, outputBeanType = Score.class)
  @Sort(query = "{ score : 1 }", order = 0)
  Page<Score> getPageableScores2(Pageable pageable);

  @Aggregate(inputType = Score.class, outputBeanType = Score.class)
  @Facet(pipelines = {
    @FacetPipeline(name="documents", stages = {
        @FacetPipelineStage(stageType = Match.class, query = "{'score' : {'$lt' : 100}}")
    })
  }, order = 0)
  @Unwind(query = "'$documents'", order = 1)
  @ReplaceRoot(query = "{" +
                        "   \"newRoot\" : \"$documents\"" +
                        "}", order = 2)
  @Sort(query = "{ score : 1 }", order = 3)
  Page<Score> getPageableWithFacet(Pageable pageable);

}