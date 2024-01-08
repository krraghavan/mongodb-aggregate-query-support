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

package com.github.krr.mongodb.aggregate.support.condition;

import com.github.krr.mongodb.aggregate.support.annotations.Conditional;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rkolliva
 * 3/8/17.
 */


public class ConditionalAnnotationMetadata implements AnnotatedTypeMetadata {

  public static final String PARAMETER_INDEX = "parameterIndex";

  private final Conditional conditional;

  public ConditionalAnnotationMetadata(Conditional conditional) {
    this.conditional = conditional;
  }

  @Override
  @NonNull public MergedAnnotations getAnnotations() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  @SuppressWarnings("squid:S1872")
  public boolean isAnnotated(@NonNull String annotationName) {
    return Conditional.class.getName().equals(annotationName);
  }

  @Override
  public Map<String, Object> getAnnotationAttributes(@NonNull String annotationName) {
    if(isAnnotated(annotationName)) {
      Map<String, Object> retval = new HashMap<>();
      retval.put(PARAMETER_INDEX, conditional.parameterIndex());
      return retval;
    }
    return null;
  }

  @Override
  public Map<String, Object> getAnnotationAttributes(@NonNull String annotationName, boolean classValuesAsString) {
    return getAnnotationAttributes(annotationName);
  }

  @Override
  public MultiValueMap<String, Object> getAllAnnotationAttributes(@NonNull String annotationName) {
    return null;
  }

  @Override
  public MultiValueMap<String, Object> getAllAnnotationAttributes(@NonNull String annotationName, boolean classValuesAsString) {
    return null;
  }
}
