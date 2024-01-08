package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Match;
import com.github.krr.mongodb.aggregate.support.beans.TestBinaryBean;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TestBinaryRepository extends MongoRepository<TestBinaryBean, String> {

  @Aggregate(name = "getByMd5Hash", inputType = TestBinaryBean.class, outputBeanType = TestBinaryBean.class)
  @Match(query = "{'md5Hash' : ?0}", order = 0)
  TestBinaryBean getByMd5Hash(byte [] md5Hash);

  TestBinaryBean findByMd5Hash(byte[] bytes);

  @Query(value = "{'md5Hash' : ?0}")
  TestBinaryBean queryByMd5Hash(byte[] bytes);
}
