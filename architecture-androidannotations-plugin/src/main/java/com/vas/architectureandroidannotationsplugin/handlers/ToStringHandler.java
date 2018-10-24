package com.vas.architectureandroidannotationsplugin.handlers;

import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.vas.architectureandroidannotations.ArcView;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.holder.EComponentHolder;

import javax.lang.model.element.Element;

public class ToStringHandler extends BaseAnnotationHandler<EComponentHolder> {

    public ToStringHandler(AndroidAnnotationsEnvironment environment) {
        super(ArcView.class, environment); // this handles your @ToString annotation
        System.out.printf("\n-----------ToStringHandler-----------\n");
    }

    @Override
    protected void validate(Element element, ElementValidation validation) {
        System.out.printf("\n-----------ToStringHandler-validate-----------\n");
        validatorHelper.enclosingElementHasEnhancedComponentAnnotation(element, validation);
        // the annotation only can be used in an enhanced class
    }

    @Override
    public void process(Element element, EComponentHolder holder) throws Exception {
        System.out.printf("\n-----------ToStringHandler-process-----------\n");
        JMethod toString = holder.getGeneratedClass().method(JMod.PUBLIC, getClasses().STRING, "toString");

        toString.body()._return(JExpr.lit("Hello, AndroidAnnotations!"));
        toString.annotate(Override.class);

        // creates a method in the generated class:
        // @Override
        // public String toString() {
        //   return "Hello, AndroidAnnotations!";
        // }
    }
}