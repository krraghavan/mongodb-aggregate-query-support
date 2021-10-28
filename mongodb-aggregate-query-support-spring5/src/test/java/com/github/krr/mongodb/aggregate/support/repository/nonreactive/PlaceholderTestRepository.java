package com.github.krr.mongodb.aggregate.support.repository.nonreactive;

import com.github.krr.mongodb.aggregate.support.annotations.Aggregate;
import com.github.krr.mongodb.aggregate.support.annotations.Match;
import com.github.krr.mongodb.aggregate.support.beans.TestAggregateAnnotation2FieldsBean;
import com.github.krr.mongodb.aggregate.support.repository.TestMongoRepository;

import java.util.List;
import java.util.Map;

public interface PlaceholderTestRepository extends TestMongoRepository<TestAggregateAnnotation2FieldsBean, String> {

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute1':\"?0\"}", order = 0)
  List<Map<String, String>> replaceSinglePlaceholderWithParameterValue(String value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'mappings.@0' : { '$exists' : true } }", order = 0)
  List<Map<String, String>> replaceSingleJsonPlaceholderOnLhsWithParameterValue(String value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute1':\"foo.?0\"}", order = 0)
  List<Map<String, String>> replaceSingleNestedPlaceholderWithParameterValue(String value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute1':\"foo.?0Id\"}", order = 0)
  List<Map<String, String>> replaceSingleNestedPlaceholderWithSuffix(String value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute1':\"foo.@0Id\"}", order = 0)

  List<Map<String, String>> replaceSingleNestedJsonPlaceholderWithSuffix(String value);
  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute1':\"$foo.@0Id\"}", order = 0)
  List<Map<String, String>> replaceSingleNestedJsonPlaceholderWithSuffixWithDlrOnField(String value);

  @Aggregate(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Map.class)
  @Match(query = "{'randomAttribute1':\"$foo.?0Id\"}", order = 0)
  List<Map<String, String>> replaceSingleNestedPlaceholderWithSuffixWithDlrOnField(String value);
}
