[![Build Status](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support.svg)](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support) [![Release Version](https://img.shields.io/badge/version-v0.7.4-red.svg)](https://github.com/krraghavan/mongodb-aggregate-query-support) [![License](https://img.shields.io/hexpm/l/plug.svg)](https://img.shields.io/hexpm/l/plug.svg)

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
