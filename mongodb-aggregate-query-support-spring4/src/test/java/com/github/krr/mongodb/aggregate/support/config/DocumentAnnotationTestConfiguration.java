package com.github.krr.mongodb.aggregate.support.config;

import com.github.krr.mongodb.aggregate.support.test.fixtures.DocumentAnnotationFixture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by anu
 * 5/19/17.
 */
@Configuration
public class DocumentAnnotationTestConfiguration {

   @Bean
   public DocumentAnnotationFixture documentAnnotationFixture() {
      return new DocumentAnnotationFixture();
   }
}
