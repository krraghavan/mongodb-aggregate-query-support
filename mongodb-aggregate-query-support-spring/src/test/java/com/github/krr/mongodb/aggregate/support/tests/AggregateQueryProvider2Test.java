/*
 *  Copyright (c) 2017 the original author or authors.
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

package com.github.krr.mongodb.aggregate.support.tests;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.beans.TestNoDocumentAnnotationBean;
import com.github.krr.mongodb.aggregate.support.beans.TestValidDocumentAnnotationBean;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.factory.AggregateQuerySupportingRepositoryFactory;
import com.github.krr.mongodb.aggregate.support.repository.TestAggregateRepository22;
import com.github.krr.mongodb.aggregate.support.test.fixtures.DocumentAnnotationFixture;
import org.bson.json.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.springframework.data.repository.query.QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;
import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 4/2/17.
 */
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateQueryProvider2Test extends AbstractTestNGSpringContextTests {

  @Autowired
  private MongoOperations mongoOperations;

  @Autowired
  private MongoQueryExecutor queryExecutor;

  @Autowired
  private QueryMethodEvaluationContextProvider evaluationContextProvider;

  @DataProvider
  private Object[][] queryMethods() {
    return new Object[][] {
        new Object[]{"aggregateQueryWithMatchOnly"},
        new Object[]{"aggregateQueryWithMultipleMatchQueries"},
        new Object[]{"aggregateQueryWithMultipleMatchQueriesInNonContiguousOrder"},
        new Object[]{"aggregateQueryWithMultipleMatchQueriesInNonContiguousOrderWithNonAggAnnotations"}
    };
  }

  // this test throws a NPE because we dont expect these queries to execute.
  @Test(dataProvider = "queryMethods", expectedExceptions = {JsonParseException.class})
  public void testCreateAggregateQuery(String methodName) throws Exception {
    AggregateQuerySupportingRepositoryFactory factory = new AggregateQuerySupportingRepositoryFactory(mongoOperations,
                                                                                                      queryExecutor);
    Optional<QueryLookupStrategy> lookupStrategyOptional = factory.getQueryLookupStrategy(CREATE_IF_NOT_FOUND,
                                                                                          evaluationContextProvider);

    QueryLookupStrategy lookupStrategy = lookupStrategyOptional.orElseThrow(() ->
                                      new IllegalArgumentException("Null query lookup strategy"));
    Method method = TestAggregateRepository22.class.getMethod(methodName);
    RepositoryMetadata repositoryMetadata = new DefaultRepositoryMetadata(TestAggregateRepository22.class);
    ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    RepositoryQuery query = lookupStrategy.resolveQuery(method, repositoryMetadata, projectionFactory, null);
    assertNotNull(query);
    Object object = query.execute(new Object[0]);
    assertNull(object);
  }

  @Test
  public void testValidDocumentAnnotationValue() {
    //Valid Spring Expression Document name validation
    String validCollectionName = mongoOperations.getCollectionName(TestValidDocumentAnnotationBean.class);
    assertNotNull(validCollectionName);
    assertEquals(DocumentAnnotationFixture.RANDOM_COLLECTION, validCollectionName);
  }

  @Test
  public void testNoDocumentAnnotationValue() {
    //No Spring Expression Document annotation specified
    //Since the expression is invalid it should pick up the class name as collection name
    String noAnnotationCollectionName = mongoOperations.getCollectionName(TestNoDocumentAnnotationBean.class);
    assertNotNull(noAnnotationCollectionName);
    assertTrue(TestNoDocumentAnnotationBean.class.getSimpleName().equalsIgnoreCase(noAnnotationCollectionName));
  }

}