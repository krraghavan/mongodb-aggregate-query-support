package com.github.krr.mongodb.aggregate.support.query;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.spel.ExpressionDependencies;
import org.springframework.data.spel.ExtensionAwareEvaluationContextProvider;
import org.springframework.expression.EvaluationContext;

@SuppressWarnings({"FieldCanBeLocal", "NullableProblems"})
public class ApplicationContextQueryMethodEvaluationContextProvider implements QueryMethodEvaluationContextProvider,
                                                                               ApplicationContextAware {

  private ExtensionAwareEvaluationContextProvider delegate;

  @Override
  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters, Object[] parameterValues) {
    return this.delegate.getEvaluationContext(parameterValues);
  }

  @Override
  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters, Object[] parameterValues,
                                                                             ExpressionDependencies dependencies) {
    return this.delegate.getEvaluationContext(parameterValues, dependencies);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.delegate = new ExtensionAwareEvaluationContextProvider(applicationContext);
  }
}
