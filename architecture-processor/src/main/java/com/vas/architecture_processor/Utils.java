package com.vas.architecture_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.api.TaskStatus;
import com.vas.architectureandroidannotations.repository.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Created by user liveData 20/09/2018.
 */
@SuppressWarnings("ALL")
public class Utils {
    public static PackageElement getPackage(Element element) {
        while (element.getKind() != ElementKind.PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element;
    }

    public static TypeElement findEnclosingTypeElement(Element e) {
        while (e != null && !(e instanceof TypeElement)) {
            e = e.getEnclosingElement();
        }
        return TypeElement.class.cast(e);
    }

    public static String getCanonicalName(TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType)) {
            return null;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return null;
        }
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (!typeArguments.isEmpty()) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');

            return typeString.toString();
        } else {
            return declaredType.toString();
        }
    }


    /**
     * @param element
     * @param classCanonicalName Full Identifier of the class {@code "java.util.Map"}{@link Map}, inner class {@code "java.util.Map.Entry"} {@link Map.Entry}
     * @return returns true if {@param typeElement} is instanceOf {@param classCanonicalName}
     */
    public static boolean instanceOf(Element element, String classCanonicalName) {
        return findInstanceOf(element, classCanonicalName) != null;
    }

    private static TypeElement findInstanceOf(Element element, String classCanonicalName) {
        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            if (typeElement.toString().equals(classCanonicalName)) {
                return typeElement;
            } else {
                TypeElement superclass = getSuperClass(typeElement);
                return superclass != null ?
                        superclass.toString().equals(classCanonicalName) ? superclass : findInstanceOf(superclass, classCanonicalName)
                        : null;
            }
        } else if (element instanceof ExecutableElement) {
            ExecutableElement typeElement = (ExecutableElement) element;
            return findInstanceOf(((DeclaredType) typeElement.getReturnType()).asElement(), classCanonicalName);
        } else if (element instanceof TypeParameterElement) {
            TypeParameterElement typeElement = (TypeParameterElement) element;
            return findInstanceOf(((DeclaredType) typeElement.asType()).asElement(), classCanonicalName);
        } else if (element instanceof VariableElement) {
            VariableElement typeElement = (VariableElement) element;
            return findInstanceOf(((DeclaredType) typeElement.asType()).asElement(), classCanonicalName);
        }
        return null;
    }

    public static TypeElement getSuperClass(TypeElement typeElement) {
        if (!(typeElement.getSuperclass() instanceof DeclaredType)) return null;
        DeclaredType declaredAncestor = (DeclaredType) typeElement.getSuperclass();
        return (TypeElement) declaredAncestor.asElement();
    }

    /**
     * @param typeElement
     * @param classCanonicalName Full Identifier of the class {@code "java.util.Map"}{@link Map}, inner class {@code "java.util.Map.Entry"} {@link Map.Entry}
     * @return returns true if {@param typeElement} is instanceOf {@param classCanonicalName}
     */
    public static boolean instanceOf(TypeElement typeElement, String classCanonicalName) {
        return findInstanceOf(typeElement, classCanonicalName) != null;
    }

    private static TypeElement findInstanceOf(TypeElement typeElement, String classCanonicalName) {
        TypeElement superclass = getSuperClass(typeElement);
        return superclass != null ?
                superclass.toString().equals(classCanonicalName) ? superclass : findInstanceOf(superclass, classCanonicalName)
                : null;
    }

    public static void generateAsyncFromMethod(TypeSpec.Builder navigatorClass, TypeMirror type, ClassName className, Element elementEnclosed, Async async) {
        ExecutableElement methodElement = (ExecutableElement) elementEnclosed;
        String methodName = methodElement.getSimpleName().toString();
        String methodTaskName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + "Task";
        TypeMirror returnType = methodElement.getReturnType();
        List<? extends VariableElement> parameterElements = methodElement.getParameters();

        boolean isVoid = returnType.getKind() == TypeKind.VOID;
        TypeName pair;
        pair = isVoid ? ParameterizedTypeName.get(ClassName.get("android.util", "Pair"),
                ClassName.get(Throwable.class),
                ClassName.get(Void.class)
        ) : ParameterizedTypeName.get(ClassName.get("android.util", "Pair"),
                ClassName.get(Throwable.class),
                ClassName.get(returnType)
        );
        TypeName asyncTask = ParameterizedTypeName.get(ClassName.get("android.os", "AsyncTask"),
                ClassName.get(Object.class),
                ClassName.get(TaskStatus.class),
                pair
        );

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
            ps.append(", $N");
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

        MethodSpec.Builder doInBackgroundMethod = MethodSpec.methodBuilder("doInBackground")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Object[].class, "objects").varargs();
        CodeBlock.Builder doInBackgroundMethodBody = CodeBlock.builder()
                .addStatement("taskStatus.setState(TaskStatus.State.RUNNING)")
                .addStatement("publishProgress(taskStatus)");
        if (isVoid) {
            doInBackgroundMethodBody
                    .beginControlFlow("try")
                    .addStatement("this.classInstance.$N($N)", methodName, parametersString.toString())
                    .addStatement("return new $T(null, null)", pair)
                    .nextControlFlow("catch ($T e)", ClassName.get(Throwable.class))
                    .addStatement("return new $T(e, null)", pair)
                    .endControlFlow();
        } else {
            doInBackgroundMethodBody
                    .beginControlFlow("try")
                    .addStatement("$T result = this.classInstance.$N($N)", returnType, methodName, parametersString.toString())
                    .addStatement("return new $T(null, result)", pair)
                    .nextControlFlow("catch ($T e)", ClassName.get(Throwable.class))
                    .addStatement("return new $T(e, null)", pair)
                    .endControlFlow();
        }

        doInBackgroundMethod.addCode(doInBackgroundMethodBody.build())
                .returns(pair);

        MethodSpec.Builder onCancelledMethod = MethodSpec.methodBuilder("onCancelled")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addCode(CodeBlock.builder()
                        .addStatement("taskStatus.setState(TaskStatus.State.CANCELLED)")
                        .addStatement("publishProgress(taskStatus)")
                        .build());
        MethodSpec.Builder onPreExecuteMethod = MethodSpec.methodBuilder("onPreExecute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addCode(CodeBlock.builder()
                        .build());
        MethodSpec.Builder onPostExecuteMethod = MethodSpec.methodBuilder("onPostExecute")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(pair, "result")
                .addCode(CodeBlock.builder()
                        .addStatement("taskStatus.finish(result.first == null " +
                                "? TaskStatus.State.SUCCEEDED " +
                                ": TaskStatus.State.FAILED, result.first)")
                        .addStatement("publishProgress(taskStatus)")
                        .addStatement("if (callback != null) callback.onFinished(result.second, result.first)")
                        .build());

        MethodSpec.Builder onProgressUpdateMethod = MethodSpec.methodBuilder("onProgressUpdate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(TaskStatus[].class, "state").varargs()
                .addStatement("if (callback != null) callback.onStateChanged(state[0])");

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(type), "classInstance")
                .addCode(CodeBlock.builder()
                        .addStatement("this.classInstance = classInstance")
                        .addStatement("publishProgress(taskStatus)")
                        .build());

        ParameterizedTypeName callbackType = ParameterizedTypeName.get(ClassName.get(Callback.class),
                isVoid ? ClassName.get(Void.class) : ClassName.get(returnType));
        FieldSpec.Builder callback = FieldSpec.builder(callbackType, "callback", Modifier.PRIVATE, Modifier.FINAL);

        FieldSpec.Builder taskStatus =
                FieldSpec.builder(ClassName.get(TaskStatus.class), "taskStatus", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T(this.getClass().getSimpleName())", ClassName.get(TaskStatus.class));

        constructor.addParameter(callbackType, "callback");
        constructor.addCode(CodeBlock.builder()
                .addStatement("this.callback = callback")
                .build());

        ClassName methodTaskClassName = className.nestedClass(methodTaskName);
        TypeSpec.Builder methodTaskClass = TypeSpec.classBuilder(methodTaskClassName.simpleName())
                .superclass(asyncTask)
                .addModifiers(Modifier.STATIC)
                .addField(FieldSpec.builder(TypeName.get(type), "classInstance")
                        .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                        .build())
                .addField(taskStatus.build())
                .addField(callback.build())
                .addMethod(constructor.build())
                .addMethod(onCancelledMethod.build())
                .addMethod(onPreExecuteMethod.build())
                .addMethod(doInBackgroundMethod.build())
                .addMethod(onPostExecuteMethod.build())
                .addMethod(onProgressUpdateMethod.build());

        navigatorClass.addType(methodTaskClass.build());


        Async.ExecutorType executorType = async.executor();
        boolean allowMultipleCalls = async.allowMultipleCalls();

        MethodSpec.Builder methodAsync = MethodSpec.methodBuilder(methodName + "Async")
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecs)
                .returns(methodTaskClassName);

        methodAsync.addParameter(callbackType, "callback");


        CodeBlock.Builder methodAsyncBody = CodeBlock.builder();
        if (!allowMultipleCalls) {
            methodAsyncBody.addStatement("$T taskG = taskMap.get($T.class.getSimpleName())",
                    ClassName.get("android.os", "AsyncTask"),
                    methodTaskClassName);
            methodAsyncBody.beginControlFlow("if (taskG != null && taskG.getStatus() != AsyncTask.Status.FINISHED)")
                    .addStatement("return ($T) taskG", methodTaskClassName)
                    .endControlFlow();
        }
        methodAsyncBody.addStatement("$T task = new $T(this, callback)", methodTaskClassName, methodTaskClassName);

        if (executorType == Async.ExecutorType.SERIAL)
            methodAsyncBody.addStatement("task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR" + ps + ")", parameterNames.toArray());
        else
            methodAsyncBody.addStatement("task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR" + ps + ")", parameterNames.toArray());

        if (!allowMultipleCalls) {
            methodAsyncBody.addStatement("taskMap.put(task.getClass().getSimpleName(), task)", methodTaskClassName, methodTaskClassName);
        }
        methodAsyncBody.addStatement("return task", methodTaskClassName, methodTaskClassName);

        methodAsync.addCode(methodAsyncBody.build());
        navigatorClass.addMethod(methodAsync.build());
    }

}
