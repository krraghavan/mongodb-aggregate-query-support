package com.github.krr.mongodb.aggregate.support.query;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.data.spel.ExpressionDependencies;
import org.springframework.expression.EvaluationContext;
import reactor.core.publisher.Mono;

@SuppressWarnings({"FieldCanBeLocal", "NullableProblems"})
public class ApplicationContextQueryMethodEvaluationContextProvider implements ReactiveQueryMethodEvaluationContextProvider,
                                                                               ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters, Object[] parameterValues) {
    throw new UnsupportedOperationException("SpEl expressions not supported");
  }

  @Override
  public <T extends Parameters<?, ?>> EvaluationContext getEvaluationContext(T parameters, Object[] parameterValues, ExpressionDependencies dependencies) {
    throw new UnsupportedOperationException("SpEl expressions not supported");
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public <T extends Parameters<?, ?>> Mono<EvaluationContext> getEvaluationContextLater(T parameters, Object[] parameterValues) {
    throw new UnsupportedOperationException("SpEl expressions not supported");
  }

  @Override
  public <T extends Parameters<?, ?>> Mono<EvaluationContext> getEvaluationContextLater(T parameters, Object[] parameterValues, ExpressionDependencies dependencies) {
    throw new UnsupportedOperationException("SpEl expressions not supported");
  }
}
