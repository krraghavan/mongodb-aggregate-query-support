package com.github.krr.mongodb.aggregate.support.beans;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by anu
 * 5/19/17.
 */
@Document(collection = "#{T(com.github.krr.mongodb.aggregate.support.test.fixtures.DocumentAnnotationFixture).RANDOM_COLLECTION}")
public class TestValidDocumentAnnotationBean extends AbstractTestAggregateBean {
   
}
