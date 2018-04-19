package com.github.krr.mongodb.aggregate.support.test.repository;

import com.github.krr.mongodb.aggregate.support.annotation.*;
import com.github.krr.mongodb.aggregate.support.test.beans.Score;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * camejavi 3/15/17
 */

public interface PageableRepository extends ReactiveMongoRepository<Score, Integer> {

  @Aggregate(inputType = Score.class, outputBeanType = Score.class, resultKey = "results")
  @Sort(query = "{ score : 1 }", order = 0)
  Flux<Score> getPageableScores(Pageable pageable);

  @Aggregate(inputType = Score.class, outputBeanType = Score.class, resultKey = "results")
  @Facet(pipelines = {
      @FacetPipeline(name = "documents", stages = {
          @FacetPipelineStage(stageType = Match.class, query = "{'score' : {'$lt' : 100}}")
      })
  }, order = 0)
  @Unwind(query = "'$documents'", order = 1)
  @ReplaceRoot(query = "{" +
                       "   \"newRoot\" : \"$documents\"" +
                       "}", order = 2)
  @Sort(query = "{ score : 1 }", order = 3)
  Flux<Score> getPageableWithFacet(Pageable pageable);

}