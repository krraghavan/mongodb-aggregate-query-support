/*
 *  Copyright (c) 2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 */
package com.cisco.spring.data.mongodb.test.config.common;

import com.cisco.spring.data.mongodb.test.beans.query.JongoQueryExecutor;
import com.cisco.spring.data.mongodb.test.beans.query.MongoQueryExecutor;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.jongo.Jongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.net.UnknownHostException;

/**
 * Created by rkolliva
 * 10/21/2015.
 */
@Configuration
public class MongoDBTestConfiguration {

    private MongoDbFactory mongoDbFactory;

    private MappingMongoConverter mongoConverter;

    private MongoTemplate mongoTemplate;

    private MongoDbFactory mongoDbFactoryForJongo;

    public MongoDBTestConfiguration() {
    }

    private Jongo jongo;

    private MongoQueryExecutor queryExecutor;

    public synchronized MongoDbFactory mongoDBFactory() throws UnknownHostException {
        if (mongoDbFactory == null) {
            mongoDbFactory = new SimpleMongoDbFactory(mongo(), "icfb");
        }
        return mongoDbFactory;
    }

    public synchronized MongoDbFactory mongoDBFactoryForJongo() throws UnknownHostException {
        if (mongoDbFactoryForJongo == null) {
            mongoDbFactoryForJongo = new SimpleMongoDbFactory(mongoForJongo(), "icfb");
        }
        return mongoDbFactoryForJongo;
    }


    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        if (mongoTemplate == null) {
            mongoTemplate = new MongoTemplate(mongoDBFactory(), mongoConverter());
            mongoTemplate.setWriteConcern(WriteConcern.JOURNALED);
        }
        return mongoTemplate;
    }

    @Bean
    public MongoClient mongo() throws UnknownHostException {
        Fongo fongo  = new Fongo("TestMongoInstance");
        return fongo.getMongo();
    }

    @Bean
    public MongoClient mongoForJongo() throws UnknownHostException {
        return mongo();
    }

    @Bean
    public MappingMongoConverter mongoConverter() throws Exception {
        if (mongoConverter == null) {
            MongoMappingContext mappingContext = new MongoMappingContext();
            DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDBFactory());
            mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
        }
        return mongoConverter;
    }

    public MongoDbFactory getMongoDbFactory() {
        return mongoDbFactory;
    }

    public void setMongoDbFactory(MongoDbFactory mongoDbFactory) {
        this.mongoDbFactory = mongoDbFactory;
    }

    public MongoDbFactory getMongoDbFactoryForJongo() {
        return mongoDbFactoryForJongo;
    }

    public void setMongoDbFactoryForJongo(MongoDbFactory mongoDbFactoryForJongo) {
        this.mongoDbFactoryForJongo = mongoDbFactoryForJongo;
    }

    @Bean
    public Jongo jongo() throws UnknownHostException {
        if(jongo == null) {
            jongo = new Jongo(mongoDBFactoryForJongo().getDb());
        }
        return jongo;
    }
    @Bean
    public MongoQueryExecutor queryExecutor() throws UnknownHostException {
        if (queryExecutor == null) {
            queryExecutor = new JongoQueryExecutor(jongo());
        }
        return queryExecutor;
    }

}
