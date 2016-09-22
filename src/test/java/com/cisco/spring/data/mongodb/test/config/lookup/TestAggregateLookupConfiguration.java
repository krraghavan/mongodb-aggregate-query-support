// Copyright (c) 2015-2016 by Cisco Systems, Inc.
/*
 *  Copyright (c) 2016 by Cisco Systems, Inc.
 *
 */

package com.cisco.spring.data.mongodb.test.config.lookup;

import com.cisco.spring.data.mongodb.test.config.common.TestMongoRepositoryConfiguration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rkolliva.
 */
@Configuration
@Import({
    TestMongoRepositoryConfiguration.class,
        })
public class TestAggregateLookupConfiguration {

}
