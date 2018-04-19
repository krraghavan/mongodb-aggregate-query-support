package com.github.krr.mongodb.aggregate.support.test.repository;

import com.github.krr.mongodb.aggregate.support.annotation.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotation.Match;
import com.github.krr.mongodb.aggregate.support.test.beans.TestBinaryBean;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface TestBinaryRepository extends ReactiveMongoRepository<TestBinaryBean, String> {

  @Aggregate(name = "getByMd5Hash", inputType = TestBinaryBean.class, outputBeanType = TestBinaryBean.class)
  @Match(query = "{'md5Hash' : ?0}", order = 0)
  Mono<TestBinaryBean> getByMd5Hash(byte[] md5Hash);

}
