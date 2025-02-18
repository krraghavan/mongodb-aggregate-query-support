package com.github.krr.mongodb.aggregate.support.query;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.query.QueryMethodValueEvaluationContextAccessor;
import org.springframework.data.spel.EvaluationContextProvider;
import org.springframework.data.spel.ExtensionAwareEvaluationContextProvider;
import org.springframework.data.spel.spi.ExtensionIdAware;

import java.util.Collection;

@SuppressWarnings({"FieldCanBeLocal", "NullableProblems"})
public class ApplicationContextQueryMethodEvaluationContextProvider extends QueryMethodValueEvaluationContextAccessor
                                                                               implements ApplicationContextAware {

  private ExtensionAwareEvaluationContextProvider delegate;

  public ApplicationContextQueryMethodEvaluationContextProvider(ApplicationContext context) {
    super(context);
  }

  public ApplicationContextQueryMethodEvaluationContextProvider(Environment environment, ListableBeanFactory beanFactory) {
    super(environment, beanFactory);
  }

  public ApplicationContextQueryMethodEvaluationContextProvider(Environment environment, EvaluationContextProvider evaluationContextProvider) {
    super(environment, evaluationContextProvider);
  }

  public ApplicationContextQueryMethodEvaluationContextProvider(Environment environment, Collection<? extends ExtensionIdAware> extensions) {
    super(environment, extensions);
  }

//  @Override
//  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters, Object[] parameterValues) {
//    return this.delegate.getEvaluationContext(parameterValues);
//  }

//  @Override
//  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters, Object[] parameterValues,
//                                                                             ExpressionDependencies dependencies) {
//    return this.delegate.getEvaluationContext(parameterValues, dependencies);
//  }
//
//  @Override
//  public EvaluationContextProvider getEvaluationContextProvider() {
//    return this.delegate.getEvaluationContextProvider();
//  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.delegate = new ExtensionAwareEvaluationContextProvider(applicationContext);
  }
}
