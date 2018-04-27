package com.github.krr.mongodb.aggregate.support.tests.nonreactive;

import com.github.krr.mongodb.aggregate.support.beans.TestBinaryBean;
import com.github.krr.mongodb.aggregate.support.config.NonReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.nonreactive.TestBinaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.DigestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@ContextConfiguration(classes = NonReactiveAggregateTestConfiguration.class)
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
//    TestBinaryBean actualBean = testBinaryRepository.findByMd5Hash(md5Hash);
//    TestBinaryBean actualBean = testBinaryRepository.queryByMd5Hash(md5Hash);
    Assert.assertNotNull(actualBean);
    Assert.assertEquals(actualBean.getMd5Hash(), binaryBean.getMd5Hash());
  }
}
