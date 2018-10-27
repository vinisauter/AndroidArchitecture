package com.vas.architecture_processor.operations;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vas.architecture_processor.Utils;
import com.vas.architecture_processor.exceptions.AnnotationException;
import com.vas.architectureandroidannotations.repository.Async;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by user liveData 04/10/2018.
 */

public class ArcRepositoryGenerator {
    Set<ClassName> classNames = new HashSet<>();

    public void generateClass(Messager messager, Filer filer, Element elementBase) throws AnnotationException, IOException {
        String pack = Utils.getPackage(elementBase).toString();
        String name = elementBase.getSimpleName().toString();
        String generatedClassName = name + "ARC";
        ClassName className = ClassName.get(pack, generatedClassName);
        classNames.add(className);
        TypeMirror type = elementBase.asType();
        TypeSpec.Builder navigatorClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC)
                .superclass(TypeName.get(type));
//        ArcRepositoryValidator.validateClass(elementBase);
        FieldSpec staticField = FieldSpec.builder(className, "instance_")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();
        navigatorClass.addField(staticField);

        navigatorClass.addMethod(MethodSpec.methodBuilder("getInstance")
                .addModifiers(PUBLIC, STATIC)
                .beginControlFlow("if (instance_ == null)")
                .addStatement("instance_ = new $T()", className)
                .endControlFlow()
                .addStatement("return instance_")
                .returns(className)
                .build());

        ParameterizedTypeName hashMapType = ParameterizedTypeName.get(ClassName.get(HashMap.class),
                ClassName.get(String.class),
                ClassName.get("android.os", "AsyncTask"));
        navigatorClass.addField(FieldSpec.builder(hashMapType, "taskMap", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new HashMap<>()")
                .build());
        for (Element elementEnclosed : elementBase.getEnclosedElements()) {
            if (elementEnclosed.getKind() == ElementKind.FIELD) {
//                ArcRepositoryValidator.validateField(elementEnclosed);
            } else if (elementEnclosed.getKind() == ElementKind.METHOD) {
//                ArcRepositoryValidator.validateMethod(elementEnclosed);
                Async async = elementEnclosed.getAnnotation(Async.class);
                if (async != null) {
                    Utils.generateAsyncFromMethod(navigatorClass, type, className, elementEnclosed, async);
                }
            }
        }
        // 3- Write generated class to a file
        JavaFile.builder(pack, navigatorClass.build()).build().writeTo(filer);
    }

    private void validateMethod(Element element) throws AnnotationException {
//            throw new AnnotationException(MessageFormat.format("{0}.{1} of type {2} may aways be final. ",
//                    element.getEnclosingElement().getSimpleName(), element.getSimpleName(), element.asType().toString()));
    }
}
