[![Build Status](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support.svg)](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support) [![Release Version](https://img.shields.io/badge/version-v0.7.14-red.svg)](https://github.com/krraghavan/mongodb-aggregate-query-support) [![License](https://img.shields.io/hexpm/l/plug.svg)](https://img.shields.io/hexpm/l/plug.svg)

# MONGO DB AGGREGATE QUERY SUPPORT
This module provides annotated support for MongoDB aggregate queries much like the @Query annotation provided by the 
Spring Data module.

The @Query annotation provided by Spring Data MongoDb allows queries to be executed with minimum code being written.  
It is highly desirable to have a similar mechanism for MongoDB aggregate queries which allow us to execute sophisticated
queries with practically no code being written.

## Current Limitations (Deprecated in v0.7.11)
Not all aggregate pipeline steps are supported.  As of 0.7.2 support is provided for
* Project
* Unwind
* Lookup
* Group
* Match
* Out
* Limit
* Bucket (Mongo 3.4+) - added in v0.7.2
* AddFields (Mongo 3.4+) - added in v0.7.2
* ReplaceRoot (Mongo 3.4+) - added in v0.7.2
* Facet (Mongo 3.4+) - added in v0.7.3
* Count - added in v0.7.4
* Skip - added in v0.7.9

## New in 0.7.14 version
Allow use of Jongo Templating mechanism (using # as a placeholder in queries).  This was needed to correctly handle 
queries that needed to be replaced with NumberLong("<a long value>").


## New in 0.7.13 version
1. Minor bug fix where conditionals were not getting ignored correctly in ```@Aggregate2``` annotated query pipelines

## New in 0.7.12 version
1. Facet pipeline stages and pipelines both support Conditional - entire pipelines or selected stages in a pipeline can
be excluded/included based on Conditions
2. Added support for query string or stages in ```FacetPipeline``` annotations.  ```query()``` and ```stages()``` are
mutually exclusive (query takes precedence)

## New in 0.7.11 version
A new set of Aggregate annotations have been created that allow the pipelines to be specified in their natural order by
leveraging Java 8 repeatable annotations.  Note that the order attribute is still required since the Java compiler 
puts all repeated annotations into their container without regard to their order in the method.  The primary purpose of
this enhancement is to improve readability of the query.  In addition to this, the specification of the Facet pipeline stage
has been changed to use nested annotations to specify the different pipeline stages.  The ```@FacetPipeline``` and 
```FacetPipelineStage``` can be used to specify multiple pipelines each with multiple stages (see unit tests for more details).
Finally, the Pageable support has been enhanced to support returning ```Page``` objects.  Users of this capability will now
 be able to get the total number of results that matched the query in addition to limiting the size of the results.

Notes
1. All annotations have the suffix 2 to distinguish them from the non-repeatable annotations - Example ```@Match2```
2. Pageable with Page return type is only supported with the new repeatable annotation
3. The non-repeatable annotations are being deprecated
4. Additional pipeline stages ```$sortByCount```, ```$graphLookup```, and ```$bucketAuto``` are supported (only in repeatable form)
Since these annotations were not supported in the previous releases, they do not have a suffix 2.
 
### Example - Pageable stage with Facet2

```
  @Aggregate2(inputType = Score.class, outputBeanType = Score.class)
  @Facet2(pipelines = {
    @FacetPipeline(name="documents", stages = {
        @FacetPipelineStage(stageType = Match2.class, query = "{'score' : {'$lt' : 100}}")
    })
  }, order = 0)
  @Unwind2(query = "'$documents'", order = 1)
  @ReplaceRoot2(query = "{" +
                        "   \"newRoot\" : \"$documents\"" +
                        "}", order = 2)
  @Sort2(query = "{ score : 1 }", order = 3)
  Page<Score> getPageableWithFacet(Pageable pageable);

```

 ### Example - Facet2 with no pageable (example in unit test is from the MongoDb 3.4 documentation)
 ```
   @Aggregate2(inputType = Artwork.class, genericType = true, outputBeanType = HashMap.class)
   @Facet2(pipelines = {
       @FacetPipeline(name = "categorizedByTags",
                      stages = {
                          @FacetPipelineStage(stageType = Unwind2.class, query = "'$tags'"),
                          @FacetPipelineStage(stageType = SortByCount.class, query = "'$tags'")
                      }),
       @FacetPipeline(name = "categorizedByPrice",
                      stages = {
                          @FacetPipelineStage(stageType = Match2.class, query = "{ price: { $exists: 1 } }"),
                          @FacetPipelineStage(stageType = Bucket2.class, query = "{" +
                                                                                 "  groupBy: \"$price\",\n" +
                                                                                 "  boundaries: [  0, 150, 200, 300, 400 ],\n" +
                                                                                 "  default: \"Other\",\n" +
                                                                                 "  output: {\n" +
                                                                                 "  \"count\": { $sum: 1 },\n" +
                                                                                 "  \"titles\": { $push: \"$title\" }\n" +
                                                                                 "  }\n" +
                                                                                 "}")
                      })}
       , order = 0)
   Map<String, Object> getFacetResults2();
 
 ```
 
 ### Example usage of different pipeline stages with repeatable annotations (the query arguments don't matter)
 ```
   @Aggregate2(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
   @Match2(query = "dont care", order = 0)
   void aggregateQueryWithMatchOnly();
 ```
 
 ```
 
   @Aggregate2(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
   @Match2(query = "dont care", order = 0)
   @Match2(query = "dont care1", order = 1)
   void aggregateQueryWithMultipleMatchQueries();
 ```
  ```

   @Aggregate2(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
   @Match2(query = "dont care", order = 0)
   @Group2(query = "dont care", order = 1)
   @Match2(query = "dont care1", order = 2)
   void aggregateQueryWithMultipleMatchQueriesInNonContiguousOrder();
 ```
 
 ```
 
   @Aggregate2(inputType = TestAggregateAnnotation2FieldsBean.class, outputBeanType = Void.class)
   @Match2(query = "dont care", order = 0)
   @Group2(query = "dont care", order = 1)
   @DummyAnnotation
   @Match2(query = "dont care1", order = 2)
   void aggregateQueryWithMultipleMatchQueriesInNonContiguousOrderWithNonAggAnnotations();

 ```
Note the actual position of the annotations don't matter but putting the annotations in their natural order improves readability.

### ADDITIONAL CHANGES
1. An additional logger was added to the QueryProvider implementations so that callers can log only the generated query
at debug levels.  These loggers are com.cisco.mongodb.aggregate.support.query.AggregateQueryProvider.Query and 
com.cisco.mongodb.aggregate.support.query.AggregateQueryProvider2.Query respectively for the ```@Aggregate``` and 
```@Aggregate2``` annotations.
2. A name attribute has been added to the ```@Aggregate``` and ```@Aggregate2``` annotations to aid debuggability.  For 
backward compatibility reasons, the name defaults to ```unnamed```.

## New in 0.7.9 version
In this version, support for the $skip pipeline stage is added along with support for Pageable in aggregate query methods.
 Using pageable will automatically add $skip and $limit pipeline stages and allows aggregate query results to be paged. 

Example (see unit test as well)

```

  @Aggregate(inputType = Score.class, outputBeanType = Score.class,
             sort = {
                 @Sort(query = "{ score : 1 }", order = 0)
             })
  List<Score> getPageableScores(Pageable pageable);

```
The ```getPageableScores``` method returns only a pageful of records from the repository.

## New in 0.7.8 version
This supports the use case where the query structure is largely the same with minor variations (say in a match stage) based
on different conditions.  In order to avoid needing to have multiple copies of queries that vary only in the definition of
one or more stages.  A new annotation and attribute ```@Conditional``` is defined that can be associated with each pipeline 
 stage (see the unit test for more details).  One or more conditional annotations can be specified and if any one of the 
 conditions match, the pipeline stage is used.  With this capability, the order attribute of each pipeline stage becomes relative.
   Stages that don't match and should not be included in the pipeline are filtered out when the pipeline is fully built.

```

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\"  : ?0," +
                                "   \"assets.cars\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
                 }),
                 @Match(query = "{" +
                                "   \"tag\": ?0," +
                                "   \"assets.homes\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
                 })
             })
  List<Possessions> mutuallyExclusiveStages(String tag, Boolean getCars, Boolean getHomes);

```
In this case, when only one of the condition matches, only the matching @Match annotation is used in the pipeline.  If
both boolean flags are true, the second match criteria will overwrite the first match (a warning is emitted to the log).
The ```ParameterValueNotNullCondition``` class returns true if the parameter identified by the index is not null.

_It is the responsibility of the user to ensure that all the pipeline stages are correct after evaluating the conditions.  Using
distinct order values so that the relative order of the stages are all correct is necessary for this functionality to work correctly.
This functionality needs more work and should be used with care._

## New in 0.7.7 version
Support for parameterized JSON objects passed in as method arguments in the form of a String.  Useful for instance when
it is desired to have the sort pipeline stage be passed in as a method argument.  Without this feature, each sort combination
will require a different interface method to be defined.  To specify sort strings (see the unit test) call the string as 
follows:

```
String sortString = "{sortTestNumber:-1}";
       List<Possessions> possessions = possessionsRepository.getPossesionsSortedByTag(tag, sortString);
```
and the repository method 
```
@Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\": ?0" +
                                "}", order = 0)
             },
             sort = {
                 @Sort(query = "\"@@1\"", order = 1)
             })
  List<Possessions> getPossesionsSortedByTag(String tag, String sort);
``` 
Note the use of the extra escaped quotes and the use of the double @@ (both required).

## New in 0.7.6 version
In some use cases it is desirable to give control of the unmarshalling process to the clients of the Aggregate query.
To achieve this the ```outputClass``` attribute of the Aggregate annotation can be set to return the DBObject type.  This
package also provides an implementation of the ResultsExtractor interface that uses the Jongo unmarshaller to return the
desired class.  However, callers are free to implement this interface and unmarshall results as they see fit.
 
## New in 0.7.5 version
Ability to parameterize keys in aggregate queries.  When the structure of the query is the same but the only difference 
is in one or more keys, it makes for better maintenance to parameterize the aggregate queries and have one
parameterized method serve multiple queries.  See the unit test for an (admittedly contrived)
example of this usage.  To parameterize the keys (as opposed to values which are parameterized by ?<n> where n is the index
 of the parameter in the method), use the notation @n (i.e use @ instead of ? to denote a placeholder).  The n in both cases
 refers to the positional index of the parameter that provides the key name in the method.

Example: (from the unit test)

```

public interface PossessionsRepository extends MongoRepository<Possessions, Integer> {

@Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"assets.@0\": { $exists: true, $ne : []}" +
                                "   }" +
                                "}", order = 0)
             })
  List<Possessions> getPossessions(String type);
  

  default boolean hasCars() {
    return CollectionUtils.isNotEmpty(getPossessions( "cars"));
  }
  
  default boolean hasHomes(String _id) {
    return CollectionUtils.isNotEmpty(getPossessions( "homes"));
  }

}
```

## Java version
Minimum Java version supported is 1.8 

## Usage
See the unit test classes and test repository definitions for examples of how to use the Aggregate query annotations.

The @Aggregate annotation is used identically to the way the Spring data @Query annotation is used.  You can put the annotation
on any interface method (with placeholders) to get Aggregate query support.  The easiest way to do this is to test the 
pipeline on a MongoDB client (like RoboMongo or MongoChef) and then copy the pipeline steps into each annotation.


# Contributors
* Kollivakkam Raghavan (owner)
* Sukhdev Singh
* Cameron Javier
* Tom Monk
