package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.factory.NonReactiveAggregateQuerySupportingRepositoryFactoryBean;
import com.github.krr.mongodb.aggregate.support.repository.nonreactive.TestAggregateRepositoryMarker;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Created by rkolliva
 * 4/28/18.
 */

@Configuration
@EnableMongoRepositories(basePackageClasses = TestAggregateRepositoryMarker.class,
                         repositoryFactoryBeanClass = NonReactiveAggregateQuerySupportingRepositoryFactoryBean.class,
                         mongoTemplateRef = "mongoOperations")

public class NonReactiveTestMongoRepositoryConfiguration {

}
