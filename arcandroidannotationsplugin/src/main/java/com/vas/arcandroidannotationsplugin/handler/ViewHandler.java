package com.vas.arcandroidannotationsplugin.handler;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JExpr;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.holder.EComponentHolder;

import javax.lang.model.element.Element;

public class ViewHandler extends BaseAnnotationHandler<EComponentHolder> {

    private final AndroidAnnotationsEnvironment environment;

    public ViewHandler(AndroidAnnotationsEnvironment environment) {
        super("com.vas.architectureandroidannotations.ViewARC", environment); // this handles your @ArcView annotation
        this.environment = environment;
    }

    @Override
    protected void validate(Element element, ElementValidation validation) {
        validatorHelper.enclosingElementIsNotAbstractIfNotAbstract(element, validation);
    }

    @Override
    public void process(Element element, EComponentHolder holder) throws Exception {
        String sFullyQualifiedClassName = element + "ARC";
        AbstractJClass clazz = environment.getCodeModel().ref(sFullyQualifiedClassName);
        holder.getInitBodyAfterInjectionBlock().staticInvoke(clazz, "init").arg(JExpr._this());
    }
}
