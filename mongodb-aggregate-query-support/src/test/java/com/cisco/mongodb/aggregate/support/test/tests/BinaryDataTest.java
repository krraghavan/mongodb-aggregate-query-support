package com.cisco.mongodb.aggregate.support.test.tests;

import com.cisco.mongodb.aggregate.support.test.beans.TestBinaryBean;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.TestBinaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.DigestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class BinaryDataTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestBinaryRepository testBinaryRepository;

  @Test
  public void mustReturnBinaryBeanQueriedWithMd5Hash() {
    TestBinaryBean binaryBean = new TestBinaryBean();
    byte[] md5Hash = DigestUtils.md5Digest(String.valueOf(System.nanoTime()).getBytes());
    binaryBean.setMd5Hash(md5Hash);
    testBinaryRepository.save(binaryBean);
    TestBinaryBean actualBean = testBinaryRepository.getByMd5Hash(md5Hash);
    Assert.assertNotNull(actualBean);
    Assert.assertEquals(actualBean.getMd5Hash(), binaryBean.getMd5Hash());
  }
}
