package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.v2.Aggregate2;
import com.cisco.mongodb.aggregate.support.annotation.v2.Match2;
import com.cisco.mongodb.aggregate.support.test.beans.TestBinaryBean;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestBinaryRepository extends MongoRepository<TestBinaryBean, String> {

  @Aggregate2(name = "getByMd5Hash", inputType = TestBinaryBean.class, outputBeanType = TestBinaryBean.class)
  @Match2(query = "{'md5Hash' : ?0}", order = 0)
  TestBinaryBean getByMd5Hash(byte[] md5Hash);

}
