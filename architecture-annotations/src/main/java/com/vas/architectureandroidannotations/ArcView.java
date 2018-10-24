package com.vas.architectureandroidannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// RetentionPolicy.:
//        SOURCE — analyses by compiler and never stored
//        CLASS — stored into class file and not retained in runtime
//        RUNTIME — store into class file and usable in runtime(by reflection)
// ElementType.:
//        TYPE, //If you want to annotate class, interface, enum..
//        FIELD, //If you want to annotate field (includes enum constants)
//        METHOD, //If you want to annotate method
//        PARAMETER, //If you want to annotate parameter
//        CONSTRUCTOR, //If you want to annotate constructor
//        LOCAL_VARIABLE, //..
//        ANNOTATION_TYPE, //..
//        PACKAGE, //..
//        TYPE_PARAMETER, //..(java 8)
//        TYPE_USE; //..(java 8)
@Retention(RetentionPolicy.CLASS) // required
@Target(ElementType.TYPE)
public @interface ArcView {
//    Class<? extends androidx.lifecycle.ViewModel> viewModel();
    Class<?> viewModel() default Void.class;;

//    Class<? extends androidx.lifecycle.ViewModelProvider.Factory> viewModelFactory() default Void.class;
    Class<?> viewModelFactory() default Void.class;
}
