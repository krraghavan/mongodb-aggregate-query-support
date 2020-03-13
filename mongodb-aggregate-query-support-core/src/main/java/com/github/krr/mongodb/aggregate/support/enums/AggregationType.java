package com.github.krr.mongodb.aggregate.support.enums;

import com.github.krr.mongodb.aggregate.support.annotations.*;

import java.lang.annotation.Annotation;

/**
 * Created by rkolliva
 * 4/25/18.
 */

public enum AggregationType {
  MATCH("$match", Match.class),
  GROUP("$group", Group.class),
  UNWIND("$unwind", Unwind.class),
  LOOKUP("$lookup", Lookup.class),
  PROJECT("$project", Project.class),
  LIMIT("$limit", Limit.class),
  BUCKET("$bucket", Bucket.class),
  ADDFIELDS("$addFields", AddFields.class),
  REPLACEROOT("$replaceRoot", ReplaceRoot.class),
  SORT("$sort", Sort.class),
  SORTBYCOUNT("$sortByCount", SortByCount.class),
  BUCKETAUTO("$bucketAuto", BucketAuto.class),
  GRAPHLOOKUP("$graphLookup", GraphLookup.class),
  FACET("$facet", Facet.class),
  COUNT("$count", Count.class),
  SKIP("$skip", Skip.class),
  OUT("$out", Out.class),
  MERGE("$merge", Merge.class)

  ;

  private final String representation;

  private final Class<? extends Annotation> annotationClass;

  AggregationType(String representation, Class<? extends Annotation> annotationClass) {
    this.representation = representation;
    this.annotationClass = annotationClass;
  }

  public static AggregationType from(Annotation annotation) {
    return from(annotation.annotationType());
  }

  public static AggregationType from(Class<? extends Annotation> stageType) {
    for (AggregationType type : values()) {
      if (type.getAnnotationClass() == stageType) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown annotation type " + stageType.getName());
  }

  public Class<? extends Annotation> getAnnotationClass() {
    return annotationClass;
  }

  public String getRepresentation() {
    return representation;
  }
}