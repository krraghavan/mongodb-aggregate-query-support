[![Build Status](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support.svg)](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support) [![Release Version](https://img.shields.io/badge/version-v0.7.5-red.svg)](https://github.com/krraghavan/mongodb-aggregate-query-support) [![License](https://img.shields.io/hexpm/l/plug.svg)](https://img.shields.io/hexpm/l/plug.svg)

# MONGO DB AGGREGATE QUERY SUPPORT
This module provides annotated support for MongoDB aggregate queries much like the @Query annotation provided by the 
Spring Data module.

The @Query annotation provided by Spring Data MongoDb allows queries to be executed with minimum code being written.  
It is highly desirable to have a similar mechanism for MongoDB aggregate queries which allow us to execute sophisticated
queries with practically no code being written.


## Current Limitations
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
  
  default boolean hasHomes(String id) {
    return CollectionUtils.isNotEmpty(getPossessions( "homes"));
  }

}
```

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
