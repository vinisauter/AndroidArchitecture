package com.vas.architectureandroidannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds ARC.init in the generated class.
 */
@Retention(RetentionPolicy.CLASS) // required
@Target(ElementType.TYPE) // this can vary per annotation
public @interface ViewARC {

}