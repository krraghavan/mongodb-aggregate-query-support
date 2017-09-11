package com.cisco.mongodb.aggregate.support.test;

import com.cisco.mongodb.aggregate.support.annotation.AggregateMetaAnnotation;
import com.cisco.mongodb.aggregate.support.annotation.v2.Count2;
import com.cisco.mongodb.aggregate.support.annotation.v2.Match2;

import java.lang.annotation.*;

/**
 * Created by ankasa
 * 8/27/17.
 */

@Retention(RetentionPolicy.RUNTIME)
@AggregateMetaAnnotation
@Match2(query = "{\n" +
                "        score: {\n" +
                "          $gt: 75\n" +
                "        }\n" +
                "      }", order = 0)
@Count2(query = "\"scores_gt_75\"", order = 1)
public @interface CountGt75AggregateAnnotationsContainer {
}
