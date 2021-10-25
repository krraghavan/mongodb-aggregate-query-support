package com.github.krr.mongodb.aggregate.support.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rkolliva
 * 4/30/18.
 */
@org.springframework.data.annotation.Id
@JacksonAnnotationsInside
@JsonProperty("_id")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MongoId {
}
