package com.github.krr.mongodb.aggregate.support.query;

import com.github.krr.mongodb.aggregate.support.api.MongoQueryExecutor;
import com.github.krr.mongodb.aggregate.support.config.NonReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.exceptions.InvalidAggregationQueryException;
import com.github.krr.mongodb.aggregate.support.factory.NonReactiveAggregateQuerySupportingRepositoryFactory;
import com.github.krr.mongodb.aggregate.support.repository.nonreactive.PlaceholderTestRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParameterAccessor;
import org.springframework.data.mongodb.repository.query.MongoParametersParameterAccessor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.repository.query.QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@ContextConfiguration(classes = NonReactiveAggregateTestConfiguration.class)
public class NonReactiveAggregateMongoQueryTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private MongoOperations mongoOperations;

  @Autowired
  private MongoQueryExecutor queryExecutor;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private QueryMethodEvaluationContextProvider evaluationContextProvider;

  @DataProvider
  public static Object[][] replaceFixtures() {
    String randomParamValue = RandomStringUtils.randomAlphabetic(10);
    return new Object[][]{
        new Object[]{"replaceSinglePlaceholderWithParameterValue",
                     randomParamValue,
                     "{$match:{'randomAttribute1':\"".concat(randomParamValue).concat("\"}}")},
        new Object[]{"replaceSingleJsonPlaceholderOnLhsWithParameterValue",
                     randomParamValue,
                     "{$match:{'mappings.".concat(randomParamValue).concat("' : { '$exists' : true } }}")},
        new Object[]{"replaceSingleNestedPlaceholderWithParameterValue",
                     randomParamValue,
                     "{$match:{'randomAttribute1':\"foo.".concat(randomParamValue).concat("\"}}")},
    };
  }

  @SuppressWarnings("ConstantConditions")
  @Test(dataProvider = "replaceFixtures")
  public void mustReplaceValueOfSingleParam(String methodName,
                                            String randomParamValue,
                                            String expectedQuery) throws NoSuchMethodException, InvalidAggregationQueryException {
    NonReactiveAggregateQuerySupportingRepositoryFactory factory = new NonReactiveAggregateQuerySupportingRepositoryFactory(mongoOperations,
                                                                                                                            queryExecutor);
    factory.setBeanFactory(applicationContext);
    Optional<QueryLookupStrategy> lookupStrategy = factory.getQueryLookupStrategy(CREATE_IF_NOT_FOUND, evaluationContextProvider);

    Assert.isTrue(lookupStrategy.isPresent(), "Lookup strategy must not be null");
    Method method = PlaceholderTestRepository.class.getMethod(methodName, String.class);
    RepositoryMetadata repositoryMetadata = new DefaultRepositoryMetadata(PlaceholderTestRepository.class);
    ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    NonReactiveAggregateMongoQuery query = (NonReactiveAggregateMongoQuery) lookupStrategy.get().resolveQuery(method, repositoryMetadata,
                                                                                                              projectionFactory, null);
    assertNotNull(query);
    MongoParameterAccessor mongoParameterAccessor = new MongoParametersParameterAccessor(query.getQueryMethod(),
                                                                                         new Object[]{randomParamValue});
    ConvertingParameterAccessor parameterAccessor = new ConvertingParameterAccessor(mongoOperations.getConverter(),
                                                                                    mongoParameterAccessor);
    AggregateQueryProvider queryProvider = (AggregateQueryProvider) query.createAggregateQueryProvider(mongoParameterAccessor, parameterAccessor);
    assertNotNull(queryProvider);
    List<String> pipelines = queryProvider.getPipelines();
    assertEquals(pipelines.size(), 1);
    String actualString = pipelines.get(0);
    assertEquals(actualString, expectedQuery);
  }
}