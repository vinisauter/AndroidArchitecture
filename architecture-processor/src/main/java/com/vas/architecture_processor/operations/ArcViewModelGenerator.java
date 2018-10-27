package com.vas.architecture_processor.operations;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

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
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC);

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

                        constructor.addStatement("super.$N = $T.getInstance()", elementEnclosed.getSimpleName(), elementEnclosed.asType());

                    }
                }
            } else if (elementEnclosed.getKind() == ElementKind.METHOD) {
                ArcValidators.validateMethod(elementEnclosed);
            }
        }
        navigatorClass.addMethod(constructor.build());

        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(ClassName.get("androidx.fragment.app", "Fragment"), "fragment")
                .addStatement("return $T.of(fragment).get($T.class)",
                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
                .returns(className)
                .build());
        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(ClassName.get("androidx.fragment.app", "FragmentActivity"), "activity")
                .addStatement("return $T.of(activity).get($T.class)",
                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
                .returns(className)
                .build());
        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(ClassName.get("androidx.fragment.app", "Fragment"), "fragment")
                .addParameter(ClassName.get("androidx.lifecycle", "ViewModelProvider", "Factory"), "factory")
                .addStatement("return $T.of(fragment, factory).get($T.class)",
                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
                .returns(className)
                .build());
        navigatorClass.addMethod(MethodSpec.methodBuilder("createInstance")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(ClassName.get("androidx.fragment.app", "FragmentActivity"), "activity")
                .addParameter(ClassName.get("androidx.lifecycle", "ViewModelProvider", "Factory"), "factory")
                .addStatement("return $T.of(activity, factory).get($T.class)",
                        ClassName.get("androidx.lifecycle", "ViewModelProviders"), className)
                .returns(className)
                .build());
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

//    /**
//     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given
//     * {@code fragment} is alive. More detailed explanation is in {@link ViewModel}.
//     * <p>
//     * It uses the given {@link ViewModelProvider.Factory} to instantiate new ViewModels.
//     *
//     * @param fragment a fragment, in whose scope ViewModels should be retained
//     * @param factory  a {@code Factory} to instantiate new ViewModels
//     * @return a ViewModelProvider instance
//     */
//    public static ConfigViewModel_ createInstance(Fragment fragment, ViewModelProvider.Factory factory) {
//        return ViewModelProviders.of(fragment, factory).get(ConfigViewModel_.class);
//    }
//
//    /**
//     * Creates a {@link ViewModelProvider}, which retains ViewModels while a scope of given Activity
//     * is alive. More detailed explanation is in {@link ViewModel}.
//     * <p>
//     * It uses the given {@link ViewModelProvider.Factory} to instantiate new ViewModels.
//     *
//     * @param activity an activity, in whose scope ViewModels should be retained
//     * @param factory  a {@code Factory} to instantiate new ViewModels
//     * @return a ViewModelProvider instance
//     */
//    public static ConfigViewModel_ createInstance(FragmentActivity activity, ViewModelProvider.Factory factory) {
//        return ViewModelProviders.of(activity, factory).get(ConfigViewModel_.class);
//    }