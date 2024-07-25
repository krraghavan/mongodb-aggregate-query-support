package com.github.krr.mongodb.aggregate.support.repository;

import com.github.krr.mongodb.aggregate.support.annotations.*;
import com.github.krr.mongodb.aggregate.support.beans.DummyBean;
import com.github.krr.mongodb.aggregate.support.condition.ParameterValueNotNullCondition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface ComplexQueryRepository extends MongoRepository<DummyBean, Integer> {

  // fixture created for bug: https://github.com/krraghavan/mongodb-aggregate-query-support/issues/50
  @Aggregate(name = "findMappingsByTypeAndVersion",
      inputType = DummyBean.class, outputBeanType = DummyBean.class)
  @Match(query = "{'_id' : '?0', }", order = 0,
      condition = {@Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 0)})
  @Project(query = "{_id: '$_id'," +
                   "              'version' : '$_id', " +
                   "                'mappings' : { " +
                   "                    '$filter' : { " +
                   "                        'input' : '$mappings', " +
                   "                        'as' : 'mappings', " +
                   "                        'cond' : { $eq: [ '$$mappings.type', '?1']  }" +
                   "                    }" +
                   "                }" +
                   "            }", order = 1,
      condition = {@Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)})
  List<DummyBean> findMappingsByTypeAndVersion(String version, MappingType type);

  enum MappingType {
    FOO,
    BAR
  }

  // fixture created for bug: https://github.com/krraghavan/mongodb-aggregate-query-support/issues/50
  // a complex query with meta annotations.
  @Aggregate(
      inputType = DummyBean.class,
      outputBeanType = DummyBean.class,
      name = "foo")
  @PrefixFilterMetaAnnotation
  @Project(
      query =
          "{"
          + "    '_id': 1,"
          + "    'maxSeverity': 1,"
          + "    'numErrors': 1,"
          + "    'prefixStr': 1,"
          + "    'prefixKey': 1,"
          + "    'prefixType': '$eventMetadata.prefixDetailsList.prefixType',"
          + "    'name': 1,"
          + "    'dn' : 1, "
          + "    'exactMatchNames1': 1,"
          + "    'exactMatchNames1': 1,"
          + "    'exactMatchNames3' : 1, "
          + "    'list1' : 1, "
          + "    'list2': 1, "
          + "    'criticalCount' : '$eventCountBySeverity.critical', "
          + "    'majorCount' : '$eventCountBySeverity.major', "
          + "    'minorCount' : '$eventCountBySeverity.minor', "
          + "    'warningCount' : '$eventCountBySeverity.warning', "
          + "    'infoCount' : '$eventCountBySeverity.info', "
          + "    'uuid' : {'$concat' : [ '?13', '-', '$md5HashHex' ] }"
          + "}",
      order = 6)
  @Sort(query = "@@10", order = 7)
  @Facet(
      pipelines = {
          @FacetPipeline(
              name = "prefixes",
              stages = {
                  @FacetPipelineStage(stageType = Skip.class, query = "?11"),
                  @FacetPipelineStage(stageType = Limit.class, query = "?12")
              }),
          @FacetPipeline(
              name = "totalResultSetCount",
              stages = {@FacetPipelineStage(stageType = Count.class, query = "'resultSetCount'")})
      },
      order = 8)
  @Unwind(query = "'$totalResultSetCount'", order = 9)
  @Project(
      query =
          "{"
          + "  'prefixes' : 1,"
          + "  'totalResultSetCount' : '$totalResultSetCount.resultSetCount'"
          + "}",
      order = 10)
  DummyBean getPrefixesAggQuery(
      Long epochWid, // 0
      String matchCriteria1, // 1
      String addFieldsStr, // 2
      String matchCriteria2, // 3
      String matchCriteria3, // 4
      int criticalSev, // 5
      int majorSev, // 6
      int minorSev, // 7
      int warningSev, // 8
      int infoSev, // 9
      String sortCriteria, // 10
      int skip, // 11
      int limit, // 12
      String uuid); // 13

  @AggregateMetaAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Match(order = 0, query = "{'abc' : '?0'}")
  @Match(
      order = 1,
      query = "@@1",
      condition = {
          @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
      })
  @AddFields(order = 2, query = "@@2")
  @AddFields(
      order = 3,
      query =
          "{"
          + "  maxSeverity: {$max : '$severities'},"
          + "  eventCountBySeverity: {"
          + "    $reduce: {"
          + "      input: '$severities',"
          + "      initialValue: {info: 0, warning: 0, minor: 0, major: 0, critical: 0},"
          + "      in: {"
          + "        critical: {$add: ['$$value.critical', {$cond: [{$eq: ['$$this', '?5']}, 1, 0]}]},"
          + "        major: {$add: ['$$value.major', {$cond: [{$eq: ['$$this', '?6']}, 1, 0]}]},"
          + "        minor: {$add: ['$$value.minor', {$cond: [{$eq: ['$$this', '?7']}, 1, 0]}]},"
          + "        warning: {$add: ['$$value.warning', {$cond: [{$eq: ['$$this', '?8']}, 1, 0]}]},"
          + "        info: {$add: ['$$value.info', {$cond: [{$eq: ['$$this', '?9']}, 1, 0]}]}"
          + "      }"
          + "    }"
          + "  }"
          + "}")
  @Match(
      order = 4,
      query = "@@3",
      condition = {
          @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 3)
      })
  @Match(
      order = 5,
      query = "@@4",
      condition = {
          @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 4)
      })
  @interface PrefixFilterMetaAnnotation {
  }
}
