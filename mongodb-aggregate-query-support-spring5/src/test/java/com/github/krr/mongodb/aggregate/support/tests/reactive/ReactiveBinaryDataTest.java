package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.github.krr.mongodb.aggregate.support.beans.TestBinaryBean;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveTestBinaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.DigestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveBinaryDataTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private ReactiveTestBinaryRepository testBinaryRepository;

  @Test
  public void mustReturnBinaryBeanQueriedWithMd5Hash() {
    testBinaryRepository.deleteAll().block();
    TestBinaryBean binaryBean = new TestBinaryBean();
    byte[] md5Hash = DigestUtils.md5Digest(String.valueOf(System.nanoTime()).getBytes());
    binaryBean.setMd5Hash(md5Hash);
    testBinaryRepository.save(binaryBean).block();
    TestBinaryBean actualBean = testBinaryRepository.getByMd5Hash(md5Hash).block();
    Assert.assertNotNull(actualBean);
    Assert.assertEquals(actualBean.getMd5Hash(), binaryBean.getMd5Hash());
  }
}
