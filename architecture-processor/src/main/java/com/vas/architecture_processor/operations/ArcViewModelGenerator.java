package com.vas.architecture_processor.operations;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vas.architecture_processor.Utils;
import com.vas.architecture_processor.exceptions.AnnotationException;
import com.vas.architectureandroidannotations.viewmodel.Repository;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Created by user liveData 04/10/2018.
 */

public class ArcViewModelGenerator {
    public HashMap<String, ArrayList<ClassName>> getLiveDataClass() {
        return liveDataClass;
    }

    HashMap<String, ArrayList<ClassName>> liveDataClass = new HashMap<>();

    public ClassName generateClass(Messager messager, Filer filer, Element elementBase) throws AnnotationException, IOException {
        String pack = Utils.getPackage(elementBase).toString();
        String name = elementBase.getSimpleName().toString();
        String generatedClassName = name + "ARC";
        ClassName className = ClassName.get(pack, generatedClassName);
        TypeMirror type = elementBase.asType();
        TypeSpec.Builder navigatorClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC)
                .superclass(TypeName.get(type));
        MethodSpec.Builder init = MethodSpec.methodBuilder("_init")
                .addModifiers(PRIVATE);

        for (Element elementEnclosed : elementBase.getEnclosedElements()) {
            ElementKind fieldKind = elementEnclosed.getKind();
            Set<Modifier> fieldModifiers = elementEnclosed.getModifiers();
            System.out.printf(MessageFormat.format(
                    "\n    EnclosedElement {0} {1} {2} {3} {4}",
                    fieldKind,
                    Arrays.toString(fieldModifiers.toArray()),
                    elementEnclosed.getSimpleName().toString(),
                    elementEnclosed.asType(),
                    elementEnclosed.asType().getKind().isPrimitive() ? "primitive" : ""));
            if (elementEnclosed.getKind() == ElementKind.FIELD) {
                if (Utils.instanceOf(elementEnclosed, "androidx.lifecycle.LiveData")) {
                    validateFieldLiveData(elementEnclosed);
                    String fieldName = elementEnclosed.getSimpleName().toString();
                    ArrayList<ClassName> listData = liveDataClass.get(fieldName);
                    if (listData == null) {
                        listData = new ArrayList<>();
                        listData.add(ClassName.get(pack, name));
                        listData.add(className);
                        liveDataClass.put(elementEnclosed.getSimpleName().toString(), listData);
                    } else {
                        listData.add(ClassName.get(pack, name));
                        listData.add(className);
                    }
                    System.out.printf("\nelem: " + elementEnclosed.getSimpleName().toString() + " - " + Arrays.toString(listData.toArray()));
                    String staticFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
                    FieldSpec staticField = FieldSpec.builder(String.class, staticFieldName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$S", fieldName)
                            .build();
                    navigatorClass.addField(staticField);
                } else {
                    Repository repository = elementEnclosed.getAnnotation(Repository.class);
                    if (repository != null) {
                        if (elementEnclosed.getModifiers().contains(PRIVATE)) {
                            messager.printMessage(Diagnostic.Kind.ERROR, MessageFormat.format("Element {0}.{1} may not be private.", elementEnclosed.getSimpleName(), elementEnclosed.getEnclosingElement().getSimpleName()));
                        }

                        init.addStatement("super.$N = $T.getInstance()", elementEnclosed.getSimpleName(), elementEnclosed.asType());
                    }
                }
            } else if (elementEnclosed.getKind() == ElementKind.METHOD) {
                ArcValidators.validateMethod(elementEnclosed);
            } else if (elementEnclosed.getKind() == ElementKind.CONSTRUCTOR) {
                if (elementEnclosed.getModifiers().contains(PUBLIC)) {
                    ExecutableElement executableElement = (ExecutableElement) elementEnclosed;
                    MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                            .addModifiers(PUBLIC);
                    List<? extends VariableElement> parameterElements = executableElement.getParameters();
                    ArrayList<ParameterSpec> parameterSpecs = new ArrayList<>();
                    ArrayList<String> parameterNames = new ArrayList<>();
                    StringBuilder ps = new StringBuilder();
                    StringBuilder parametersString = new StringBuilder();
                    if (parameterElements.size() > 0) {
                        VariableElement firstParameter = parameterElements.get(0);
                        String fpName = firstParameter.getSimpleName().toString();
                        parameterNames.add(fpName);
                        TypeMirror fpTypeName = firstParameter.asType();
                        parameterSpecs.add(ParameterSpec.builder(TypeName.get(fpTypeName), fpName).build());
                        parametersString.append("(").append(fpTypeName).append(") objects[").append(0).append("]");
                        ps.append("$N");
                        for (int i = 1; i < parameterElements.size(); i++) {
                            VariableElement variableElement = parameterElements.get(i);
                            String pName = variableElement.getSimpleName().toString();
                            parameterNames.add(fpName);
                            TypeMirror pTypeName = variableElement.asType();
                            parameterSpecs.add(ParameterSpec.builder(TypeName.get(pTypeName), pName).build());
                            parametersString.append(", (").append(pTypeName).append(") objects[").append(i).append("]");
                            ps.append(", $N");
                        }
                    }
                    constructor.addParameters(parameterSpecs);
                    constructor.addStatement("super(" + ps + ")", parameterNames.toArray());
                    constructor.addStatement("_init()");
                    navigatorClass.addMethod(constructor.build());
                }
            }
        }
        navigatorClass.addMethod(init.build());

//        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
//                .addModifiers(PUBLIC, STATIC)
//                .addParameter(ClassName.get("androidx.fragment.app", "Fragment"), "fragment")
//                .addStatement("return $T.of(fragment).get($T.class)",
//                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
//                .returns(className)
//                .build());
//        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
//                .addModifiers(PUBLIC, STATIC)
//                .addParameter(ClassName.get("androidx.fragment.app", "FragmentActivity"), "activity")
//                .addStatement("return $T.of(activity).get($T.class)",
//                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
//                .returns(className)
//                .build());
//        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
//                .addModifiers(PUBLIC, STATIC)
//                .addParameter(ClassName.get("androidx.fragment.app", "Fragment"), "fragment")
//                .addParameter(ClassName.get("androidx.lifecycle", "ViewModelProvider", "Factory"), "factory")
//                .addStatement("return $T.of(fragment, factory).get($T.class)",
//                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
//                .returns(className)
//                .build());
//        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
//                .addModifiers(PUBLIC, STATIC)
//                .addParameter(ClassName.get("androidx.fragment.app", "FragmentActivity"), "activity")
//                .addParameter(ClassName.get("androidx.lifecycle", "ViewModelProvider", "Factory"), "factory")
//                .addStatement("return $T.of(activity, factory).get($T.class)",
//                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
//                .returns(className)
//                .build());
        // 3- Write generated class to a file
        JavaFile.builder(pack, navigatorClass.build()).build().writeTo(filer);
        return className;
    }

    private void validateFieldLiveData(Element element) throws AnnotationException {
        if (!element.getModifiers().contains(FINAL)) {
            throw new AnnotationException(MessageFormat.format("{0}.{1} of type {2} may aways be final. ",
                    element.getEnclosingElement().getSimpleName(), element.getSimpleName(), element.asType().toString()));
        }
    }
}