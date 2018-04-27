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

package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.factory.ReactiveAggregateQuerySupportingRepositoryFactory;
import com.github.krr.mongodb.aggregate.support.beans.TestNoDocumentAnnotationBean;
import com.github.krr.mongodb.aggregate.support.beans.TestValidDocumentAnnotationBean;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.fixtures.DocumentAnnotationFixture;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveTestAggregateRepository22;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveTestNoDocumentAnnotationRepository;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveTestValidDocumentAnnotationRepository;
import org.bson.json.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.repository.query.QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;
import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 4/2/17.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveAggregateQueryProviderTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private ReactiveMongoOperations mongoOperations;

  @Autowired
  private MongoQueryExecutor queryExecutor;

  @Autowired
  private EvaluationContextProvider evaluationContextProvider;

  @Autowired
  private ReactiveTestValidDocumentAnnotationRepository testValidDocumentAnnotationRepository;

  @Autowired
  private ReactiveTestNoDocumentAnnotationRepository testNoDocumentAnnotationRepository;

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
    ReactiveAggregateQuerySupportingRepositoryFactory factory = new ReactiveAggregateQuerySupportingRepositoryFactory(mongoOperations,
                                                                                                                      queryExecutor);
    Optional<QueryLookupStrategy> lookupStrategy = factory.getQueryLookupStrategy(CREATE_IF_NOT_FOUND, evaluationContextProvider);

    Assert.isTrue(lookupStrategy.isPresent(), "Lookup strategy must not be null");
    Method method = ReactiveTestAggregateRepository22.class.getMethod(methodName);
    RepositoryMetadata repositoryMetadata = new DefaultRepositoryMetadata(ReactiveTestAggregateRepository22.class);
    ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    RepositoryQuery query = lookupStrategy.get().resolveQuery(method, repositoryMetadata, projectionFactory, null);
    assertNotNull(query);
    Object object = query.execute(new Object[0]);
    assertNull(object);
  }

  @Test
  public void testValidDocumentAnnotationValue() {
    // add a document to the collection to make sure collection gets created.
    TestValidDocumentAnnotationBean bean = new TestValidDocumentAnnotationBean();
    mongoOperations.insert(bean).block();
    //Valid Spring Expression Document name validation
    Mono<Boolean> validCollectionName = mongoOperations.collectionExists(DocumentAnnotationFixture.RANDOM_COLLECTION);
    List<String> collectionNames = mongoOperations.getCollectionNames().collectList().block();
    assertTrue(collectionNames.contains(DocumentAnnotationFixture.RANDOM_COLLECTION));
  }

  @Test
  public void testNoDocumentAnnotationValue() {
    TestNoDocumentAnnotationBean bean = new TestNoDocumentAnnotationBean();
    mongoOperations.insert(bean).block();
    //No Spring Expression Document annotation specified
    //Since the expression is invalid it should pick up the class name as collection name
    Boolean noAnnotationCollectionNameExists = mongoOperations.collectionExists(TestNoDocumentAnnotationBean.class).block();
    assertTrue(noAnnotationCollectionNameExists);
    List<String> collectionNames = mongoOperations.getCollectionNames().collectList().block();
    assertTrue(collectionNames.contains("testNoDocumentAnnotationBean"));
//    assertTrue(TestNoDocumentAnnotationBean.class.getSimpleName().equalsIgnoreCase(noAnnotationCollectionNameExists));
  }

}