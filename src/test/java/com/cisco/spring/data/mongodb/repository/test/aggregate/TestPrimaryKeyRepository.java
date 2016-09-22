// Copyright (c) 2015-2016 by Cisco Systems, Inc.
/*
 *  Copyright (c) 2016 by Cisco Systems, Inc.
 *
 */

package com.cisco.spring.data.mongodb.repository.test.aggregate;

import com.cisco.spring.data.mongodb.test.beans.TestPrimaryKeyBean;
import com.cisco.spring.data.mongodb.test.beans.annotation.Aggregate;
import com.cisco.spring.data.mongodb.test.beans.annotation.Lookup;

import java.util.List;

/**
 * @author rkolliva.
 */
public interface TestPrimaryKeyRepository extends TestMongoRepository<TestPrimaryKeyBean, String> {

  @Aggregate(inputType = TestPrimaryKeyBean.class,
      lookup = {@Lookup(query = "{" +
                                "                \"from\": 'testForeignKeyBean'," +
                                "                \"localField\" : \"_id\"," +
                                "                \"foreignField\": \"foreignKey\"," +
                                "                \"as\": \"foreignKeyBeanList\"" +
                                "            }", order = 0)},
      outputBeanType = TestPrimaryKeyBean.class,
      genericType = true
  )
  List<TestPrimaryKeyBean> findAllPrimaryKeyBeans();
}
