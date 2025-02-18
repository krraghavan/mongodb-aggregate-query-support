package com.github.krr.mongodb.aggregate.support.query;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ReactiveExtensionAwareQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.data.spel.EvaluationContextProvider;
import org.springframework.data.spel.ExpressionDependencies;
import org.springframework.expression.EvaluationContext;
import reactor.core.publisher.Mono;

@SuppressWarnings({"FieldCanBeLocal", "NullableProblems"})
public class ApplicationContextQueryMethodEvaluationContextProvider implements ReactiveQueryMethodEvaluationContextProvider,
                                                                               ApplicationContextAware {

  private ReactiveExtensionAwareQueryMethodEvaluationContextProvider delegate;

  @Override
  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters, Object[] parameterValues) {
    return this.delegate.getEvaluationContext(parameters, parameterValues);
  }

  @Override
  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters,
                                                                             Object[] parameterValues,
                                                                             ExpressionDependencies dependencies) {
    return this.delegate.getEvaluationContext(parameters, parameterValues, dependencies);
  }

  @Override
  public EvaluationContextProvider getEvaluationContextProvider() {
    return this.delegate.getEvaluationContextProvider();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.delegate = new ReactiveExtensionAwareQueryMethodEvaluationContextProvider(applicationContext);
  }

  @Override
  public <T extends Parameters<?, ?>> Mono<EvaluationContext> getEvaluationContextLater(T parameters,
                                                                                        Object[] parameterValues) {
    return this.delegate.getEvaluationContextLater(parameters, parameterValues);
  }

  @Override
  public <T extends Parameters<?, ?>> Mono<EvaluationContext> getEvaluationContextLater(T parameters,
                                                                                        Object[] parameterValues,
                                                                                        ExpressionDependencies dependencies) {
    return this.delegate.getEvaluationContextLater(parameters, parameterValues, dependencies);
  }
}
