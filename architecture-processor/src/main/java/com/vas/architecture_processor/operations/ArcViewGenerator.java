package com.vas.architecture_processor.operations;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vas.architecture_processor.Utils;
import com.vas.architecture_processor.exceptions.AnnotationException;
import com.vas.architectureandroidannotations.view.ObserveData;
import com.vas.architectureandroidannotations.view.ViewModel;

import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by user liveData 04/10/2018.
 */

public class ArcViewGenerator {
    Set<ClassName> classNames = new HashSet<>();

    public void generateClass(Messager messager, Filer filer, Element elementBase, HashMap<String, ClassName> generatedViewModels, HashMap<String, ArrayList<ClassName>> liveDataClass) throws AnnotationException, IOException {
//        ArcViewModelValidator.validateClass(elementBase);

        String pack = Utils.getPackage(elementBase).toString();
        String name = elementBase.getSimpleName().toString();
        String generatedClassName = name + "ARC";
        ClassName className = ClassName.get(pack, generatedClassName);
        classNames.add(className);

        TypeMirror type = elementBase.asType();
        TypeSpec.Builder navigatorClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC)
//                .superclass(TypeName.get(type))
                ;
        MethodSpec.Builder init = MethodSpec.methodBuilder("init")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(TypeName.get(elementBase.asType()), "view");

        ArrayList<ExecutableElement> observeDataMethods = new ArrayList<>();
        HashMap<String, List<String>> vmsFromView = new HashMap<>();
        for (Element elementEnclosed : elementBase.getEnclosedElements()) {
            if (elementEnclosed.getKind() == ElementKind.FIELD) {
                ViewModel viewModel = elementEnclosed.getAnnotation(ViewModel.class);
                if (viewModel != null) {
                    String vmClassName = ((DeclaredType) elementEnclosed.asType()).asElement().getSimpleName().toString();
                    ClassName vmTypeName = generatedViewModels.get(vmClassName);
                    System.out.printf(MessageFormat.format("\n    Instantiated: {0} of {1}", elementEnclosed.getSimpleName().toString(), vmClassName));
                    if (vmTypeName == null)
                        throw new AnnotationException("Did not find ViewModel for " + vmClassName + " " + elementEnclosed.getSimpleName().toString());
                    init.addStatement("view.$N = $T.of(view).get($T.class)", elementEnclosed.getSimpleName(), ClassName.get("androidx.lifecycle", "ViewModelProviders"), vmTypeName);

                    List<String> listData = vmsFromView.get(vmTypeName.simpleName());
                    if (listData == null) {
                        listData = new ArrayList<>();
                        listData.add(elementEnclosed.getSimpleName().toString());
                        vmsFromView.put(vmTypeName.simpleName(), listData);
                    } else {
                        listData.add(elementEnclosed.getSimpleName().toString());
                    }
                }
            } else if (elementEnclosed.getKind() == ElementKind.METHOD) {
                ObserveData observeData = elementEnclosed.getAnnotation(ObserveData.class);
                if (observeData != null) {
                    observeDataMethods.add((ExecutableElement) elementEnclosed);
                }
            }
        }
        for (ExecutableElement methodElement : observeDataMethods) {
            ObserveData observeData = methodElement.getAnnotation(ObserveData.class);

            List<? extends VariableElement> parameters = methodElement.getParameters();
            if (parameters.size() != 1) {
                throw new AnnotationFormatError("Annotation ObserveData must be in a method with a single parameter");
            }
            VariableElement parameter = parameters.get(0);

            String liveDataName = observeData.liveData();
            if (liveDataName.isEmpty()) {
                liveDataName = parameter.getSimpleName().toString();
            }
            String viewModelName = observeData.viewModel();
//            Class clazz = Void.class;
//            clazz.getSimpleName();

            if (viewModelName.isEmpty()) {
                ArrayList<String> liveDataFromViewForLiveDataName = new ArrayList<>();
                ArrayList<ClassName> classVMNamesProj = liveDataClass.get(liveDataName);
                if (classVMNamesProj != null) {
                    for (ClassName projClassName : classVMNamesProj) {
                        List<String> sujVM = vmsFromView.get(projClassName.simpleName());
                        if (sujVM != null) {
                            liveDataFromViewForLiveDataName.addAll(sujVM);
                        }
                    }
                }
                if (liveDataFromViewForLiveDataName.size() > 0) {
                    viewModelName = liveDataFromViewForLiveDataName.get(0);
                    if (viewModelName != null && liveDataFromViewForLiveDataName.size() != 1) {
                        messager.printMessage(Diagnostic.Kind.WARNING, "Found more than one LiveData for "
                                + liveDataName + ": " + liveDataFromViewForLiveDataName.toString() + " use \"viewModel\" to specify which ViewModel should be used: " +
                                "@ObserveData(viewModel = \"XXX\", liveData = \"" + liveDataName + "\") replace XXX with one of " + liveDataFromViewForLiveDataName.toString()
                        );
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Did not found LiveData named " + liveDataName + " on any ViewModels of " + pack + "." + name);
                }
            }

            String methodName = methodElement.getSimpleName().toString();
            TypeName observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                    ClassName.get(parameter.asType())
            );

            System.out.printf(MessageFormat.format("\n    Observing: {0}.{1}", viewModelName, liveDataName));

            if (viewModelName != null && !viewModelName.isEmpty() && !liveDataName.isEmpty()) {
                init.addStatement("view.$N.$N.observe(view, $N)", viewModelName, liveDataName, TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(observer)
                        .addMethod(MethodSpec.methodBuilder("onChanged")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(ClassName.get(parameter.asType()), "item")
                                .addStatement("view.$N(item)", methodName)
                                .build())
                        .build().toString());
            }
        }


        navigatorClass.addMethod(init.build());
        // 3- Write generated class to a file
        JavaFile.builder(pack, navigatorClass.build()).build().writeTo(filer);
    }

    /**
     * public static void viewInit(MainActivity activity) {
     * activity.viewVM = ViewModelProviders.of(activity).get(MainViewModel_.class);
     * activity.viewVM.currentUser.observeForever(new Observer<User>() {
     *
     * @Override public void onChanged(User user) {
     * activity.onUserChanged(user);
     * }
     * });
     * }
     */

    private void createInstancesForViewModel(TypeSpec.Builder navigatorClass, TypeName className) throws AnnotationException {
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
    }

    private void validateFieldLiveData(Element element) throws AnnotationException {
        if (!element.getModifiers().contains(FINAL)) {
            throw new AnnotationException(MessageFormat.format("{0}.{1} of type {2} may aways be final. ",
                    element.getEnclosingElement().getSimpleName(), element.getSimpleName(), element.asType().toString()));
        }
    }

    private void validateMethod(Element element) throws AnnotationException {
//            throw new AnnotationException(MessageFormat.format("{0}.{1} of type {2} may aways be final. ",
//                    element.getEnclosingElement().getSimpleName(), element.getSimpleName(), element.asType().toString()));
    }
}