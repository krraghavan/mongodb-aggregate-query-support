package com.github.krr.mongodb.aggregate.support.repository.reactive;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Match;
import com.github.krr.mongodb.aggregate.support.beans.TestBinaryBean;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveTestBinaryRepository extends ReactiveMongoRepository<TestBinaryBean, String> {

  @Aggregate(name = "getByMd5Hash", inputType = TestBinaryBean.class, outputBeanType = TestBinaryBean.class)
  @Match(query = "{'md5Hash' : ?0}", order = 0)
  Mono<TestBinaryBean> getByMd5Hash(byte[] md5Hash);

}
