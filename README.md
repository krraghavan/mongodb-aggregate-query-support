[![Java CI with Maven](https://github.com/krraghavan/mongodb-aggregate-query-support/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/krraghavan/mongodb-aggregate-query-support/actions/workflows/maven.yml)[![Release Version](https://img.shields.io/badge/version-v0.9.2-red.svg)](https://github.com/krraghavan/mongodb-aggregate-query-support) [![License](https://img.shields.io/hexpm/l/plug.svg)](https://img.shields.io/hexpm/l/plug.svg)

# MONGO DB AGGREGATE QUERY SUPPORT
This module provides annotated support for MongoDB aggregate queries much like the @Query annotation provided by the 
Spring Data module.

The @Query annotation provided by Spring Data MongoDb allows queries to be executed with minimum code being written.  
It is highly desirable to have a similar mechanism for MongoDB aggregate queries which allow us to execute sophisticated
queries with practically no code being written.

## New in 0.9.2 version
1. Fixed date parsing in extended Bson parsing, longs, and ISO string formats with and without msecs is now correctly deserialized.  See [Mongo Extended BSON Date](https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/#mongodb-bsontype-Date)
   1. Date in long format: ```{"$date":{"$numberLong":"1710005987717"}}```
   2. Date as ISO String (msec): ```{"$date":"2024-03-09T09:39:47,717Z"}```
   3. Date as ISO String (no msec): ```{"$date":"2024-03-09T09:39:47Z"}```
2. Upgraded ```Spring Core``` version to ```6.1.4```, ```Spring Data``` to ```3.2.3``` and ```Spring Data MongoDb``` to ```4.2.3``` to address critical vulnerabilities 

## New in 0.9.1 version
1. Fixed multiple bugs in placeholder handling.
   1. @ placeholders which are used to specify keys dynamically e.g. ``` {"@1" : "value"}```"
   2. @@ placeholders (which specify the entire query string in the parameter) are now supported on Match stages.  Placeholder values that map to {} return null
   3. @@ and @ placeholders can only be strings

## New in 0.9.0 version
1. Upgraded Spring support to Spring 6.x and Mongo to 6.x+.  With this release version, we will end support for Spring 4
and earlier version of Mongo DB.  Those will continue to be supported on the 0.8.x train.
2. Tested with Java version 17 for source and target and it works but retaining Java 8 to support clients that have not
yet upgraded.

## New in 0.8.9 version
1. Fixed Mongo distribution for all Linux distributions (Thanks Siddharth Agrawal)

## New in 0.8.7 version
1. Add support for Merge annotation introduced in 4.2
1. Extend Embedded Mongo to be able to use 4.2 Mongo (original Embedded Mongo no longer supports it)

## New in 0.8.6 version
1. Addressed security vulnerability in Jackson libraries (upgraded to 2.10.1)
1. Upgraded spring-mongo version to 2.2.1
1. Upgraded to Mongo Java driver version 3.11.2 and 4.x Mongo DB

## New in 0.8.5 version
1. Addressed security vulnerability in Jackson libraries (upgraded to 2.9.7)

## New in 0.8.4 version
***** DO NOT USE THIS VERSION.  IT WAS INCORRECTLY BUILT AND DEPLOYED.  USE 0.8.4 *****

## New in 0.8.3 version
***** DO NOT USE THIS VERSION.  IT WAS INCORRECTLY BUILT AND DEPLOYED.  USE 0.8.4 *****

## New in 0.8.2 version
1. Fixed issue where Query provider's _allowDisk_ and _maxTimeout_ parameters were not being considered in aggregate queries.
2. Made object mapper injectable to allow custom deserializations to be used.

## New in 0.8.1 version
This is a completely refactored version which is incompatible with the 0.7.x family of releases.  The following changes 
have been made
1. Removed Jongo as a dependency completely.  Even though the Mongo Java driver POJO has support for POJO serialization
and deserialization it has some restrictions.  Accordingly Jackson is used to deserialize query responses into POJOs.
Custom deserializers for the extended Json syntax of Mongo support this capability.  To handle generics, the outputClass
annotation can be omitted (which returns a Document from the query) and it can be deserialized using the BsonDocumentObjectMapper. 
1. Removed support for the deprecated Aggregate annotation and renamed Aggregate2 to Aggregate.  Functionality
of this renamed Aggregate annotation is identical to the Aggregate2 annotation in 0.7.
1. The Spring4 module is built with Spring Brussels-SR9 Spring platform release train.  All Spring and Spring Data
dependencies are marked optional so nothing will be transitively inherited from this module.  Users
of this module will need to make sure that compatible Spring releases are used.  The reactive support is built with
Spring Cairo.RELEASE release train for Spring 5.x and Spring Data MongoDb 2.x support.
1. Mongo Java driver 3.6.3 for non-reactive and 1.7.1 reactive streams Java driver are supported/tested

### Reactive and Non-Reactive Usage
When the reactive module is used, both reactive and non-reactive queries can be performed by using the appropriate
Factory and query executor classes.  For Reactive Aggregation queries use the 
```ReactiveAggregateQuerySupportingRepositoryFactoryBean``` and the ```ReactiveMongoNativeJavaDriverExecutor```.  The 
Spring configuration class to enable this support looks like this
```
@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = ReactiveTestAggregateRepositoryMarker.class,
                                 repositoryFactoryBeanClass = ReactiveAggregateQuerySupportingRepositoryFactoryBean.class,
                                 reactiveMongoTemplateRef = "reactiveMongoOperations")
public class ReactiveTestMongoRepositoryConfiguration {

}

```
Check the unit tests for more examples of how to use both reactive and non-reactive repositories with the reactive module. 

To include the reactive aggregate query support with Maven use
```
<dependency>
  <groupId>com.github.krraghavan</groupId>
  <artifactId>mongodb-aggregate-query-support-reactive</artifactId>
  <version>0.8.5</version>
</dependency>
```

For Spring 4 use (no reactive support with Spring 4)
```
<dependency>
  <groupId>com.github.krraghavan</groupId>
  <artifactId>mongodb-aggregate-query-support-spring4</artifactId>
  <version>0.8.5</version>
</dependency>
```

## New in 0.7.27 version
Added support for the Reactive Mongo Java driver using the io-reactor implementation.  All the aggregate query annotations now have 
a reactive counterpart in the mongodb-aggregate-query-support-reactive module.  In doing this change we use the native 
Bson POJO support to serialize/deserialize the query response.  With this, jongo is no longer required as a dependency.
Jongo dependency in the non-reactive version will also be removed in an upcoming release.  The reactive module does not
support the old aggregate annotations. 

## New in 0.7.26 version
Added a new annotation CollectionName that can be used on one parameter of an Aggregate2 annotated method to specify the name of the collection on which the aggregate query will be executed.  This allows the aggregate query to be specified on the interface method while allowing the collection name to be dynamically specified at runtime.  The parameter annotated with CollectionName can be of any type and the toString() method of that parameter will be called to derive the name of the collection. One use case where this could be useful is when a documents of a certain type are stored in multiple different collections (perhaps for performance reasons).

## New in 0.7.25 version
Add the ability to set timeout on aggregate queries.

## New in 0.7.24 version
Change logging dependencies scope so they don't have to be excluded from projects consuming this library

## New in 0.7.23 version
Conditional inclusion of pipeline stages can now specify ANY or ALL conditions to match allowing AND and OR logic to be used.  For ANY the stage is included if at least one condition matches.  For ALL, all conditions specified in the conditional must evaluate to true.

## New in 0.7.22 version
Change allowDiskUse to true by default for Aggregate queries

## New in 0.7.21 version
Added the ability to modify a pipeline before executing it.

## New in 0.7.20 version
Added a meta annotation ```@AggregateMetaAnnotation``` that can be used to combine commonly used pipeline query structures and 
promote reuse. 

## New in 0.7.19 version
Facet2 was not repeatable - this is now fixed.

## New in 0.7.18 version
Upgraded to Spring Data MongoDb 1.10.x and Spring 4.3.8

## New in 0.7.17 version
Added support to use disk for aggregate queries that exceed sort memory limit of 100MB

## New in 0.7.16 version
1. Use strict mode JSON serializer so that binary data gets serialized correctly by Mongo Java driver

## New in 0.7.15 version
Remove bad 0.7.14 build changes.
Add support for expressions in Document annotations

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
* Anusha Kasa
* Dulanjalie Ganegedara
* Chetan Narsude
* Siddharth Agrawal

