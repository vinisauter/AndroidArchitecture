package com.vas.architecture_processor.operations;

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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
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
//                    String staticFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
//                    FieldSpec staticField = FieldSpec.builder(String.class, staticFieldName)
//                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
//                            .initializer("$S", fieldName)
//                            .build();
//                    navigatorClass.addField(staticField);
                    TypeName observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                            ClassName.get(Utils.getGenericType(elementEnclosed.asType()))
                    );
                    MethodSpec.Builder liveDataField = MethodSpec.methodBuilder(fieldName)
                            .addModifiers(PUBLIC)
                            .addParameter(ClassName.get("androidx.lifecycle", "LifecycleOwner"), "owner")
                            .addParameter(observer, "observer")
                            .addStatement("super.$N.observe(owner, observer)", fieldName);
                    navigatorClass.addMethod(liveDataField.build());
                } else {
                    Repository repository = elementEnclosed.getAnnotation(Repository.class);
                    if (repository != null) {
                        if (elementEnclosed.getModifiers().contains(PRIVATE)) {
                            messager.printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(" --- Element {0}.{1} may not be private.", elementEnclosed.getSimpleName(), elementEnclosed.getEnclosingElement().getSimpleName()));
                        }
                        String rClassName = ((DeclaredType) elementEnclosed.asType()).asElement().getSimpleName().toString();
                        ClassName rTypeName = generatedRepositories.get(rClassName);
                        if (rTypeName == null)
                            throw new AnnotationException("Did not find Repository for " + rClassName + " " + elementEnclosed.getSimpleName().toString());
                        init.addStatement("super.$N = $T.getInstance()", elementEnclosed.getSimpleName(), rTypeName);
                    }
                }
            } else if (elementEnclosed.getKind() == ElementKind.CONSTRUCTOR) {
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
            } else if (elementEnclosed.getKind() == ElementKind.METHOD) {
//                ArcValidators.validateMethod(elementEnclosed);
                ExecutableElement methodElement = (ExecutableElement) elementEnclosed;
                String methodName = methodElement.getSimpleName().toString();
                Element returnElement = ArchitectureProcessor.pEnvironment.getTypeUtils().asElement(methodElement.getReturnType());
                boolean returnsLiveData = Utils.instanceOf(returnElement, "androidx.lifecycle.LiveData");
                if (returnsLiveData) {
                    MethodSpec.Builder liveDataMethod = MethodSpec.methodBuilder(methodName)
                            .addModifiers(PUBLIC);

                    TypeName observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                            ClassName.get(Utils.getGenericType(methodElement.getReturnType()))
                    );
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
                            parametersString.append(", ").append(fpName);
                        }
                    }
                    if (parameterSpecs.size() > 0)
                        liveDataMethod.addParameters(parameterSpecs);
                    liveDataMethod.addParameter(ClassName.get("androidx.lifecycle", "LifecycleOwner"), "owner")
                            .addParameter(observer, "observer")
                            .addStatement("super.$N(" + parametersString + ").observe(owner, observer)", methodName);
                    navigatorClass.addMethod(liveDataMethod.build());
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