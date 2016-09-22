// Copyright (c) 2015-2016 by Cisco Systems, Inc.
package com.cisco.spring.data.mongodb.test.config.common;

import com.cisco.spring.data.mongodb.repository.test.aggregate.TestAggregateRepositoryMarker;
import com.cisco.spring.data.mongodb.test.beans.factory.AggregateQuerySupportingRepositoryFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Created by rkolliva
 * 10/20/2015.
 */
@Configuration
@EnableMongoRepositories(basePackageClasses = TestAggregateRepositoryMarker.class,
                         repositoryFactoryBeanClass = AggregateQuerySupportingRepositoryFactoryBean.class)
public class TestMongoRepositoryConfiguration {

    public TestMongoRepositoryConfiguration() {

    }
}
