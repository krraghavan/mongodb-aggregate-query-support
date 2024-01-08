package com.github.krr.mongodb.aggregate.support;

import com.github.krr.mongodb.aggregate.support.annotations.AggregateMetaAnnotation;
import com.github.krr.mongodb.aggregate.support.annotations.Count;
import com.github.krr.mongodb.aggregate.support.annotations.Match;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ankasa
 * 8/27/17.
 */

@Retention(RetentionPolicy.RUNTIME)
@AggregateMetaAnnotation
@Match(query = "{\n" +
               "        score: {\n" +
               "          $gt: 75\n" +
               "        }\n" +
               "      }", order = 0)
@Count(query = "\"scores_gt_75\"", order = 1)
public @interface CountGt75AggregateAnnotationsContainer {
}
