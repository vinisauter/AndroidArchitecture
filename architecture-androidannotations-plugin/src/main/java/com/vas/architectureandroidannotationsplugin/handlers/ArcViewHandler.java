package com.vas.architectureandroidannotationsplugin.handlers;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJAssignmentTarget;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.IJStatement;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;
import com.vas.architectureandroidannotations.ArcView;
import com.vas.architectureandroidannotations.view.ObserveData;
import com.vas.architectureandroidannotations.view.ViewModel;
import com.vas.architectureandroidannotationsplugin.validators.ArcViewValidator;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.handler.MethodInjectionHandler;
import org.androidannotations.helper.InjectHelper;
import org.androidannotations.holder.EComponentHolder;

import java.lang.annotation.AnnotationFormatError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class ArcViewHandler extends BaseAnnotationHandler<EComponentHolder> implements MethodInjectionHandler<EComponentHolder> {
    private final InjectHelper<EComponentHolder> injectHelper;

    public ArcViewHandler(AndroidAnnotationsEnvironment environment) {
        super(ArcView.class, environment);
        System.out.printf("\n-----------ArcViewHandler-----------\n");
        injectHelper = new InjectHelper<>(validatorHelper, this);
    }

    @Override
    protected void validate(Element element, ElementValidation validation) {
        validatorHelper.enclosingElementHasEnhancedComponentAnnotation(element, validation);
//        if (validatorHelper.elementHasAnnotation(EActivity.class, element)
//                || validatorHelper.elementHasAnnotation(EFragment.class, element)
//                || validatorHelper.elementHasAnnotation(EView.class, element)
//                || validatorHelper.elementHasAnnotation(EViewGroup.class, element)
//                ) {
//            validatorHelper.isNotPrivate(element, validation);
//            validatorHelper.isNotFinal(element, validation);
//        }
        System.out.printf("\n-----------ArcViewHandler-validate-----------\n");
    }

    @Override
    public void process(Element element, EComponentHolder holder) throws Exception {
        injectHelper.process(element, holder);
        System.out.printf("\n-----------ArcViewHandler-process-----------\n");
        System.out.printf("\n" + element.getSimpleName() + " AnnotationMirrors: " + element.getAnnotationMirrors().toString());
    }

    @Override
    public JBlock getInvocationBlock(EComponentHolder holder) {
        return holder.getInitBodyInjectionBlock();
    }

    @Override
    public void assignValue(JBlock targetBlock, IJAssignmentTarget fieldRef, EComponentHolder holder, Element element, Element param) {
        // the InjectHelper will call this function
        // * targetBlock is a block in the init-method (which is returned by @getInvocationBlock)
        // * fieldRef is the field reference (this.bus) that is annotated with this ViewModel annotation
        // * element is the java-model element (which refers to the annotated element/field)
        System.out.printf("\n" + element.getSimpleName() + "\nAnnotationMirrors: " + element.getAnnotationMirrors().toString());
        HashMap<Class, ArrayList<Element>> annHashMap = new HashMap<>();
        for (Element elementEnclosed : element.getEnclosedElements()) {
            if (elementEnclosed.getKind() == ElementKind.FIELD) {
                ArcViewValidator.validateElementField(elementEnclosed);
                ViewModel viewModel = elementEnclosed.getAnnotation(ViewModel.class);
                if (viewModel != null) {
                    ArrayList<Element> list = annHashMap.get(ViewModel.class);
                    if (list == null) {
                        list = new ArrayList<>();
                        list.add(elementEnclosed);
                        annHashMap.put(ViewModel.class, list);
                    } else
                        list.add(elementEnclosed);
                }
            } else if (elementEnclosed.getKind() == ElementKind.METHOD) {
                ArcViewValidator.validateElementMethod(elementEnclosed);
                ObserveData observeData = elementEnclosed.getAnnotation(ObserveData.class);
                if (observeData != null) {
                    ArrayList<Element> list = annHashMap.get(ObserveData.class);
                    if (list == null) {
                        list = new ArrayList<>();
                        list.add(elementEnclosed);
                        annHashMap.put(ObserveData.class, list);
                    } else
                        list.add(elementEnclosed);
                }
            }
        }
        ArrayList<Element> viewModelList = annHashMap.get(ViewModel.class);
        ArrayList<Element> observerList = annHashMap.get(ObserveData.class);

        for (Element elementEnclosed : viewModelList) {
            String fieldName = elementEnclosed.getSimpleName().toString();
            JFieldRef viewModelRef = JExpr._super().ref(fieldName);
            VariableElement variableElement = (VariableElement) elementEnclosed;
            initViewModelMember(targetBlock, viewModelRef, variableElement);
        }
        for (Element elementEnclosed : observerList) {
            ExecutableElement methodElement = (ExecutableElement) elementEnclosed;
            List<? extends VariableElement> parameters = methodElement.getParameters();
            if (parameters.size() != 1) {
                throw new AnnotationFormatError("Annotation ObserveData must be in a method with a single parameter");
            }
            VariableElement parameter = parameters.get(0);

            ObserveData observeData = elementEnclosed.getAnnotation(ObserveData.class);
//            String viewModelName = viewModelNames.get(observeData.viewModel());
            String viewModelName = observeData.viewModel();

            String liveDataName = observeData.liveData();
            if (liveDataName.isEmpty())
                liveDataName = parameter.getSimpleName().toString();
            String methodName = elementEnclosed.getSimpleName().toString();
            String parameterTypeName = parameter.asType().toString();
            String observerName = "androidx.lifecycle.Observer";
            String observerFieldName = "observer" + liveDataName.substring(0, 1).toUpperCase() + liveDataName.substring(1);

            AbstractJClass parameterTypeClass = getJClass(parameterTypeName);
            AbstractJClass viewModelProvidersClass = getJClass(observerName).narrow(parameterTypeClass);

            JCodeModel codeModel = new JCodeModel();
            JDefinedClass anonymousFactory = codeModel.anonymousClass(viewModelProvidersClass);

            JMethod create = anonymousFactory.method(JMod.PUBLIC, Void.TYPE, "onChanged");
            create.annotate(Override.class);
            JVar itemParam = create.param(parameterTypeClass, "item");

            create.body().add(JExpr.invoke(methodName).arg(itemParam));

            IJExpression newObserver = JExpr._new(anonymousFactory);
            JFieldVar observerVar = holder.getGeneratedClass().field(JMod.PUBLIC | JMod.FINAL, viewModelProvidersClass, observerFieldName, newObserver);
            JFieldRef liveDataRef = JExpr._super().ref(viewModelName).ref(liveDataName);
            JInvocation invocation = liveDataRef.invoke("observe").arg(JExpr._this()).arg(observerVar);

            targetBlock.add(invocation);
        }
    }

    /**
     * @param targetBlock the code block where the initialization code is added to
     * @param element     the annotated element
     */
    private void initViewModelMember(JBlock targetBlock, JFieldRef fieldRef, VariableElement element) {
        TypeMirror fieldType = element.asType();
        String fieldTypeQualifiedName = "androidx.lifecycle.ViewModelProviders";
        AbstractJClass viewModelProvidersClass = getJClass(fieldTypeQualifiedName);
        AbstractJClass fieldTypeClass = getJClass(fieldType.toString());
        IJExpression expression = viewModelProvidersClass
                .staticInvoke("of")
                .arg(JExpr._this())
                .invoke("get")
                .arg(fieldTypeClass.dotclass());

        IJStatement statement = fieldRef.assign(expression);
        targetBlock.add(statement);
    }

    @Override
    public void validateEnclosingElement(Element element, ElementValidation valid) {
//        List<Class<? extends Annotation>> validAnnotations = asList(EActivity.class, EViewGroup.class, EView.class, EFragment.class);
//        boolean shouldFind = true;
//        boolean foundAnnotation = false;
//        for (Class<? extends Annotation> validAnnotation : validAnnotations) {
//            if (element.getAnnotation(validAnnotation) != null) {
//                foundAnnotation = true;
//                break;
//            }
//        }
//
//        if (!foundAnnotation) {
//            valid.addError(element, "%s can only be used in a " + element.getKind().toString().toLowerCase()  + " annotated with.");
//        }
//        validatorHelper.enclosingElementHasEnhancedComponentAnnotation(element, valid);
    }
}