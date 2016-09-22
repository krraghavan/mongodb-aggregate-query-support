// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.repository.test.aggregate;

import com.cisco.spring.data.mongodb.test.beans.AbstractTestAggregateBean;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;

/**
 * Created by rkolliva on 10/20/2015.
 */
public interface TestMongoRepository<T extends AbstractTestAggregateBean, ID extends Serializable>
    extends MongoRepository<T, ID> {
}
