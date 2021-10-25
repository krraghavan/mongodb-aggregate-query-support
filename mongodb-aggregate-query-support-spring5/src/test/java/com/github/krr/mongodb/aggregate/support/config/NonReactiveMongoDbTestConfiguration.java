package com.github.krr.mongodb.aggregate.support.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rkolliva
 * 4/28/18.
 */

@Configuration
public class NonReactiveMongoDbTestConfiguration {

  @Bean
  public String mongoDbName() {
    return RandomStringUtils.randomAlphabetic(7);
  }
}
