package com.vas.architecture_processor.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vas.architecture_processor.Utils;
import com.vas.architecture_processor.exceptions.AnnotationException;
import com.vas.architectureandroidannotations.ViewARC;
import com.vas.architectureandroidannotations.view.ObserveData;
import com.vas.architectureandroidannotations.view.ViewModel;

import java.io.IOException;
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

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by user liveData 04/10/2018.
 */

@SuppressWarnings("UnusedReturnValue")
public class ArcViewGenerator {
    private Set<ClassName> classNames = new HashSet<>();

    public Set<ClassName> generateClass(Messager messager, Filer filer, Element elementBase, HashMap<String, ClassName> generatedViewModels, HashMap<String, ArrayList<ClassName>> liveDataClass) throws AnnotationException, IOException {
//        ArcViewModelValidator.validateClass(elementBase);

        ViewARC viewARC = elementBase.getAnnotation(ViewARC.class);
        boolean isActivity = Utils.instanceOf(elementBase, "androidx.fragment.app.FragmentActivity");
        boolean isFragment = Utils.instanceOf(elementBase, "androidx.fragment.app.Fragment");
        boolean isView = Utils.instanceOf(elementBase, "android.view.View");

        String pack = Utils.getPackage(elementBase).toString();
        String name = elementBase.getSimpleName().toString();
        String generatedClassName = name + "ARC";
        ClassName className = ClassName.get(pack, generatedClassName);
        classNames.add(className);

        TypeSpec.Builder navigatorClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC);
        MethodSpec.Builder init = MethodSpec.methodBuilder("init")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(TypeName.get(elementBase.asType()), "view", FINAL);

        if (isActivity) {
            init.addStatement("$T owner = view", TypeName.get(elementBase.asType()));
        } else {
            boolean isSharedVM = viewARC.useSharedVM();
            if (isFragment) {
                if (isSharedVM) {
                    ClassName fragmentActivity = ClassName.get("androidx.fragment.app", "FragmentActivity");
                    init.addStatement("$T owner = view.getActivity()", fragmentActivity);
                } else {
                    init.addStatement("$T owner = view", TypeName.get(elementBase.asType()));
                }
            } else if (isView) {
//                boolean isLifecycleOwner = Utils.instanceOf(elementBase, "androidx.lifecycle.LifecycleOwner");
//                if (!isLifecycleOwner) {
//                    messager.printMessage(Diagnostic.Kind.NOTE, " --- ArcView " + elementBase.getSimpleName() + " must implement androidx.lifecycle.LifecycleOwner! ---\n");
//                }

                // ViewModel
                init.addStatement("FragmentActivity owner = getActivity(view.getContext())");
                init.addStatement("if (owner == null) throw new RuntimeException(\"View owner must be an Activity\");");
                ClassName fragmentActivity = ClassName.get("androidx.fragment.app", "FragmentActivity");
                ClassName context = ClassName.get("android.content", "Context");

                navigatorClass.addMethod(MethodSpec.methodBuilder("getActivity")
                        .addModifiers(PUBLIC, STATIC)
                        .addParameter(context, "context", FINAL)
                        .addStatement("if (context == null) return null")
                        .beginControlFlow("else if (context instanceof $T)", ClassName.get("android.content", "ContextWrapper"))
                        .addStatement("if (context instanceof FragmentActivity) return (FragmentActivity) context")
                        .addStatement("else return getActivity(((ContextWrapper) context).getBaseContext())")
                        .endControlFlow()
                        .addStatement("return null")
                        .returns(fragmentActivity)
                        .build());
            } else {
                ClassName fragmentActivity = ClassName.get("androidx.fragment.app", "FragmentActivity");
                init.addStatement("$T owner = view.getActivity()", fragmentActivity);
            }
        }

        ArrayList<ExecutableElement> observeDataMethods = new ArrayList<>();
        HashMap<String, List<String>> vmsFromView = new HashMap<>();
        for (Element elementEnclosed : elementBase.getEnclosedElements()) {
            if (elementEnclosed.getKind() == ElementKind.FIELD) {
                ViewModel viewModel = elementEnclosed.getAnnotation(ViewModel.class);
                if (viewModel != null) {
                    ClassName vmTypeName = viewModelInstanceGenerator(generatedViewModels, generatedClassName, init, elementEnclosed);
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
            observerGenerator(messager, liveDataClass, pack, name, generatedClassName, init, vmsFromView, methodElement, observeData);
        }

        navigatorClass.addMethod(init.build());
        // 3- Write generated class to a file
        JavaFile.builder(pack, navigatorClass.build()).build().writeTo(filer);
        return classNames;
    }

    private ClassName viewModelInstanceGenerator(HashMap<String, ClassName> generatedViewModels, String generatedClassName, MethodSpec.Builder init, Element elementEnclosed) throws AnnotationException {
        String vmClassName = ((DeclaredType) elementEnclosed.asType()).asElement().getSimpleName().toString();
        ClassName vmTypeName = generatedViewModels.get(vmClassName);
        Utils.logInfo(MessageFormat.format("{0} Instantiated: {1} = {2}", generatedClassName, elementEnclosed.getSimpleName().toString(), vmClassName));
        if (vmTypeName == null) {
            Utils.logError("Did not find ViewModel for " + vmClassName + " " + elementEnclosed.getSimpleName().toString());
        }else {
            init.addStatement("view.$N = $T.of(owner).get($T.class)", elementEnclosed.getSimpleName(), ClassName.get("androidx.lifecycle", "ViewModelProviders"), vmTypeName);
        }
        return vmTypeName;
    }

    private void observerGenerator(Messager messager, HashMap<String, ArrayList<ClassName>> liveDataClass, String pack, String name, String generatedClassName, MethodSpec.Builder init, HashMap<String, List<String>> vmsFromView, ExecutableElement methodElement, ObserveData observeData) {
        List<? extends VariableElement> parameters = methodElement.getParameters();
        if (parameters.size() != 1) {
            Utils.logError("Annotation ObserveData must be in a method with a single parameter");
            return;
        }
        VariableElement parameter = parameters.get(0);

        String liveDataName = observeData.liveData();
        if (liveDataName.isEmpty()) {
            liveDataName = parameter.getSimpleName().toString();
        }
        String viewModelName = observeData.viewModel();

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
                    Utils.logError(" --- Found more than one LiveData for "
                            + liveDataName + ": " + liveDataFromViewForLiveDataName.toString() + " use \"viewModel\" to specify which ViewModel should be used: " +
                            "@ObserveData(viewModel = \"XXX\", liveData = \"" + liveDataName + "\") replace XXX with one of " + liveDataFromViewForLiveDataName.toString()
                    );
                    return;
                }
            } else {
                Utils.logError(" --- Did not found LiveData named " + liveDataName + " on any ViewModels of " + pack + "." + name);
                return;
            }
        }

        String methodName = methodElement.getSimpleName().toString();
        TypeName observer = ParameterizedTypeName.get(ClassName.get("androidx.lifecycle", "Observer"),
                ClassName.get(parameter.asType())
        );

        Utils.logInfo(MessageFormat.format("{0} Observing: {1}.{2}", generatedClassName, viewModelName, liveDataName));

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

//    /**
//     * public static void viewInit(MainActivity activity) {
//     * activity.viewVM = ViewModelProviders.of(activity).get(MainViewModel_.class);
//     * activity.viewVM.currentUser.observeForever(new Observer<User>() {
//     *
//     * @Override public void onChanged(User user) {
//     * activity.onUserChanged(user);
//     * }
//     * });
//     * }
//     */
//    private void createInstancesForViewModel(TypeSpec.Builder navigatorClass, TypeName className) throws AnnotationException {
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
//    }

//    private void validateFieldLiveData(Element element) throws AnnotationException {
//        if (!element.getModifiers().contains(FINAL)) {
//            throw new AnnotationException(MessageFormat.format("{0}.{1} of type {2} may aways be final. ",
//                    element.getEnclosingElement().getSimpleName(), element.getSimpleName(), element.asType().toString()));
//        }
//    }

//    private void validateMethod(Element element) throws AnnotationException {
//        throw new AnnotationException(MessageFormat.format("{0}.{1} of type {2} may aways be final. ",
//                element.getEnclosingElement().getSimpleName(), element.getSimpleName(), element.asType().toString()));
//    }
}