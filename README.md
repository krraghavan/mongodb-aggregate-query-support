[![Build Status](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support?branch=master)](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support)

# MONGO DB AGGREGATE QUERY SUPPORT
This module provides annotated support for MongoDB aggregate queries much like the @Query annotation provided by the 
Spring Data module.

The @Query annotation provided by Spring Data MongoDb allows queries to be executed with minimum code being written.  
It is highly desirable to have a similar mechanism for MongoDB aggregate queries which allow us to execute sophisticated
queries with practically no code being written.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fakemongo/fongo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.fakemongo/fongo/) ![License Apache2](https://go-shields.herokuapp.com/license-apache2-blue.png) 

## Current Limitations
Not all aggregate pipeline steps are supported.  As of 1.0.0 support is provided for

* Project
* Unwind
* Lookup
* Group
* Match
* Out
* Limit

Minimum Java version supported is 1.8 

## Usage
See the test classes and test repository definitions for examples of how to use the Aggregate query annotations.

# Contributors
* Kollivakkam Raghavan (owner)
* Sukhdev Singh