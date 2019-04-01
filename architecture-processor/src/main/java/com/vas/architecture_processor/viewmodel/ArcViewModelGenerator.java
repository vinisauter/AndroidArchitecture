package com.vas.architecture_processor.viewmodel;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vas.architecture_processor.ArchitectureProcessor;
import com.vas.architecture_processor.Utils;
import com.vas.architecture_processor.exceptions.AnnotationException;
import com.vas.architectureandroidannotations.viewmodel.Repository;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

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

    private HashMap<String, ArrayList<ClassName>> liveDataClass = new HashMap<>();

    public ClassName generateClass(Messager messager, Filer filer, Element elementBase, HashMap<String, ClassName> generatedRepositories) throws AnnotationException, IOException {
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
            if (elementEnclosed.getKind() == ElementKind.FIELD) {
                Repository repository = elementEnclosed.getAnnotation(Repository.class);
                if (repository != null) {
                    repositoryInstanceGenerator(messager, generatedRepositories, init, elementEnclosed);
                } else if (Utils.instanceOf(elementEnclosed, "androidx.lifecycle.LiveData")) {
                    liveDataFieldGenerator(messager, pack, name, className, navigatorClass, elementEnclosed);
                }
            } else if (elementEnclosed.getKind() == ElementKind.CONSTRUCTOR) {
                viewModelConstructorGenerator(navigatorClass, elementEnclosed);
            } else if (elementEnclosed.getKind() == ElementKind.METHOD) {
                //ArcValidators.validateMethod(elementEnclosed);
                ExecutableElement methodElement = (ExecutableElement) elementEnclosed;
                String methodName = methodElement.getSimpleName().toString();
                boolean returnsLiveData = Utils.instanceOf(methodElement.getReturnType(), "androidx.lifecycle.LiveData");
                if (returnsLiveData) {
                    liveDataMethodGenerator(navigatorClass, methodElement, methodName);
                }
            }
        }
        navigatorClass.addMethod(init.build());
        // 3- Write generated class to a file
        JavaFile.builder(pack, navigatorClass.build()).build().writeTo(filer);
        return className;
    }

    private void viewModelConstructorGenerator(TypeSpec.Builder navigatorClass, Element elementEnclosed) {
        if (elementEnclosed.getModifiers().contains(PUBLIC)) {
            ExecutableElement executableElement = (ExecutableElement) elementEnclosed;
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC);
            List<? extends VariableElement> parameterElements = executableElement.getParameters();
            ArrayList<ParameterSpec> parameterSpecs = new ArrayList<>();
            ArrayList<String> parameterNames = new ArrayList<>();
            StringBuilder ps = new StringBuilder();
            if (parameterElements.size() > 0) {
                VariableElement firstParameter = parameterElements.get(0);
                String fpName = firstParameter.getSimpleName().toString();
                parameterNames.add(fpName);
                TypeMirror fpTypeName = firstParameter.asType();
                parameterSpecs.add(ParameterSpec.builder(TypeName.get(fpTypeName), fpName).build());
                ps.append("$N");
                for (int i = 1; i < parameterElements.size(); i++) {
                    VariableElement variableElement = parameterElements.get(i);
                    String pName = variableElement.getSimpleName().toString();
                    parameterNames.add(fpName);
                    TypeMirror pTypeName = variableElement.asType();
                    parameterSpecs.add(ParameterSpec.builder(TypeName.get(pTypeName), pName).build());
                    ps.append(", $N");
                }
            }
            constructor.addParameters(parameterSpecs);
            constructor.addStatement("super(" + ps + ")", parameterNames.toArray());
            constructor.addStatement("_init()");
            navigatorClass.addMethod(constructor.build());
        }
    }

    private void liveDataMethodGenerator(TypeSpec.Builder navigatorClass, ExecutableElement methodElement, String methodName) {
        MethodSpec.Builder liveDataMethod = MethodSpec.methodBuilder(methodName)
                .addModifiers(PUBLIC);
        TypeName observer = generateObserverFor(methodElement.getReturnType(), "method - " + methodElement.getSimpleName().toString());
        if (observer == null)
            return;
        List<? extends VariableElement> parameterElements = methodElement.getParameters();
        ArrayList<ParameterSpec> parameterSpecs = new ArrayList<>();
        StringBuilder parametersString = new StringBuilder();
        if (parameterElements.size() > 0) {
            VariableElement firstParameter = parameterElements.get(0);
            String fpName = firstParameter.getSimpleName().toString();
            TypeMirror fpTypeName = firstParameter.asType();
            parameterSpecs.add(ParameterSpec.builder(TypeName.get(fpTypeName), fpName).build());
            parametersString.append(fpName);
            for (int i = 1; i < parameterElements.size(); i++) {
                VariableElement variableElement = parameterElements.get(i);
                String pName = variableElement.getSimpleName().toString();
                TypeMirror pTypeName = variableElement.asType();
                parameterSpecs.add(ParameterSpec.builder(TypeName.get(pTypeName), pName).build());
                parametersString.append(", ").append(pName);
            }
        }
        if (parameterSpecs.size() > 0)
            liveDataMethod.addParameters(parameterSpecs);
        liveDataMethod.addParameter(ClassName.get("androidx.lifecycle", "LifecycleOwner"), "owner")
                .addParameter(observer, "observer")
                .addStatement("super.$N(" + parametersString + ").observe(owner, observer)", methodName);
        navigatorClass.addMethod(liveDataMethod.build());
    }

    private void liveDataFieldGenerator(Messager messager, String pack, String name, ClassName className, TypeSpec.Builder navigatorClass, Element elementEnclosed) throws AnnotationException {
        if (!elementEnclosed.getModifiers().contains(FINAL)) {
            Utils.logError((MessageFormat.format("{0}.{1} of type {2} may aways be final. ",
                    elementEnclosed.getEnclosingElement().getSimpleName(), elementEnclosed.getSimpleName(), elementEnclosed.asType().toString())));
            return;
        }
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
        TypeName observer = generateObserverFor(elementEnclosed.asType(), "field - " + elementEnclosed.getSimpleName().toString());
        if (observer == null)
            return;
        MethodSpec.Builder liveDataField = MethodSpec.methodBuilder(fieldName)
                .addModifiers(PUBLIC)
                .addParameter(ClassName.get("androidx.lifecycle", "LifecycleOwner"), "owner")
                .addParameter(observer, "observer")
                .addStatement("super.$N.observe(owner, observer)", fieldName);
        navigatorClass.addMethod(liveDataField.build());
    }

    private TypeName generateObserverFor(TypeMirror liveDataTypeMirror, String simpleName) {
        TypeName observer = null;
        if (liveDataTypeMirror.toString().contains("androidx.lifecycle.LiveData")
                || liveDataTypeMirror.toString().contains("androidx.lifecycle.MutableLiveData")) {
            observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                    ClassName.get(Utils.getGenericType(liveDataTypeMirror)[0])
            );
            Utils.logInfo("T lv1->" + simpleName + " " + observer);
        } else {
            HashMap<String, TypeMirror> typeDefinition = new HashMap<>();
            TypeMirror typeDef = liveDataTypeMirror;
            TypeMirror typeGen = ArchitectureProcessor.pEnvironment.getTypeUtils().asElement(typeDef).asType();
            while (!typeDef.toString().contains("androidx.lifecycle.LiveData")) {
                TypeMirror[] paramsDef = Utils.getGenericType(typeDef);
                TypeMirror[] paramsGen = Utils.getGenericType(typeGen);
                TypeElement elDef = (TypeElement) ((DeclaredType) typeDef).asElement();
                TypeElement elDefSuper = (TypeElement) ((DeclaredType) elDef.getSuperclass()).asElement();
                for (int i = 0; i < paramsGen.length; i++) {
                    TypeMirror paramGen = paramsGen[i];
                    TypeMirror paramDef = paramsDef[i];
                    typeDefinition.put(elDefSuper.getSimpleName() + "." + paramGen, paramDef);
                }
                typeDef = elDef.getSuperclass();
            }
            if (!typeDefinition.keySet().isEmpty()) {
                TypeElement elDef = (TypeElement) ((DeclaredType) typeDef).asElement();
                TypeMirror parameterizedTFromLiveDataDef = Utils.getGenericType(typeDef)[0];
                String key = elDef.getSimpleName() + "." + parameterizedTFromLiveDataDef;
                TypeMirror tFromLiveData = typeDefinition.get(key);
                if (tFromLiveData != null) {
                    TypeName cnParams = ClassName.get(tFromLiveData);
                    observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                            cnParams);
                    Utils.logInfo("T lv2->" + simpleName + " " + observer);
                } else {
                    TypeMirror parameterizedTFromLiveDataGen = ArchitectureProcessor.pEnvironment.getTypeUtils()
                            .asElement(parameterizedTFromLiveDataDef).asType();
                    TypeMirror[] paramsDef = Utils.getGenericType(parameterizedTFromLiveDataDef);
                    TypeMirror[] paramsGen = Utils.getGenericType(parameterizedTFromLiveDataGen);
                    TypeElement el2Def = (TypeElement) ((DeclaredType) typeDef).asElement();
                    for (int i = 0; i < paramsGen.length; i++) {
                        TypeMirror paramGen = paramsGen[i];
                        TypeMirror paramDef = paramsDef[i];
                        String keyP = el2Def.getSimpleName() + "." + paramDef;
                        TypeMirror alreadyContains = typeDefinition.get(keyP);
                        if (alreadyContains != null) {
                            typeDefinition.put(el2Def.getSimpleName() + "." + paramGen, alreadyContains);
                        } else {
                            typeDefinition.put(el2Def.getSimpleName() + "." + paramGen, paramDef);
                        }
                    }
                    TypeName[] cnParams = new TypeName[paramsGen.length];
                    for (int i = 0; i < paramsGen.length; i++) {
                        cnParams[i] = ClassName.get(typeDefinition.get(el2Def.getSimpleName() + "." + paramsGen[i]));
                    }
                    if (cnParams.length > 0) {
                        observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                                ParameterizedTypeName.get(ClassName.get((TypeElement) ((DeclaredType) parameterizedTFromLiveDataDef).asElement()), cnParams)
                        );
                    } else {
                        observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                                ClassName.get((TypeElement) ((DeclaredType) parameterizedTFromLiveDataDef).asElement())
                        );
                    }
                    Utils.logInfo("T lv3->" + simpleName + " " + observer);
                }
            }
        }
        return observer;
    }

    private static void repositoryInstanceGenerator(Messager messager, HashMap<String, ClassName> generatedRepositories, MethodSpec.Builder init, Element elementEnclosed) throws AnnotationException {
        if (elementEnclosed.getModifiers().contains(PRIVATE)) {
            Utils.logError(MessageFormat.format(" --- Element {0}.{1} may not be private.", elementEnclosed.getSimpleName(), elementEnclosed.getEnclosingElement().getSimpleName()));
            return;
        }
        String rClassName = ((DeclaredType) elementEnclosed.asType()).asElement().getSimpleName().toString();
        ClassName rTypeName = generatedRepositories.get(rClassName);
        if (rTypeName == null) {
            Utils.logError("Did not find Repository for " + rClassName + " " + elementEnclosed.getSimpleName().toString());
            return;
        }
        init.addStatement("super.$N = $T.getInstance()", elementEnclosed.getSimpleName(), rTypeName);
    }

}