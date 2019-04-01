package com.vas.architecture_processor.repository.annotations;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vas.architectureandroidannotations.repository.Async;
import com.vas.architectureandroidannotations.repository.AsyncType;
import com.vas.architectureandroidannotations.repository.ExecutorType;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.vas.architecture_processor.Utils.callbackClassName;
import static com.vas.architecture_processor.Utils.lifecyclePack;
import static com.vas.architecture_processor.Utils.stateClassName;
import static com.vas.architecture_processor.Utils.taskStatusClassName;

public class AsyncGenerator {
    private static final String rxPackage = "io.reactivex";
    private static final ClassName completableCN = ClassName.get(rxPackage, "Completable");
    private static final ClassName observableCN = ClassName.get(rxPackage, "Observable");
    private static final ClassName androidSchedulersCN = ClassName.get(rxPackage + ".android.schedulers", "AndroidSchedulers");
    private static final ClassName schedulersCN = ClassName.get(rxPackage + ".schedulers", "Schedulers");

    public static void generateAsyncFromMethod(TypeSpec.Builder navigatorClass, TypeMirror type, ClassName className, Element elementEnclosed, Async async) {
        AsyncType asyncType = async.value();
        ExecutorType executorType = async.executor();
        boolean allowMultipleCalls = async.allowMultipleCalls();
        ExecutableElement methodElement = (ExecutableElement) elementEnclosed;
        String methodName = methodElement.getSimpleName().toString();
        String methodTaskName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + "Task";
        TypeMirror returnType = methodElement.getReturnType();
        boolean isVoid = returnType.getKind() == TypeKind.VOID;
        TypeName returnTypeName = isVoid ? ClassName.get(Void.class) : ClassName.get(returnType);
        List<? extends VariableElement> parameterElements = methodElement.getParameters();

        ArrayList<ParameterSpec> parameterSpecs = new ArrayList<>();
        ArrayList<String> parameterNames = new ArrayList<>();
        StringBuilder parametersString = new StringBuilder();
        StringBuilder parametersNString = new StringBuilder();
        StringBuilder parametersArrayPosString = new StringBuilder();
        if (parameterElements.size() > 0) {
            VariableElement firstParameter = parameterElements.get(0);
            String fpName = firstParameter.getSimpleName().toString();
            parameterNames.add(fpName);
            TypeMirror fpTypeName = firstParameter.asType();
            parameterSpecs.add(ParameterSpec.builder(TypeName.get(fpTypeName), fpName).build());
            parametersArrayPosString.append("(").append(fpTypeName).append(") objects[").append(0).append("]");
            parametersNString.append("$N");
            parametersString.append(fpName);
            for (int i = 1; i < parameterElements.size(); i++) {
                VariableElement variableElement = parameterElements.get(i);
                String pName = variableElement.getSimpleName().toString();
                parameterNames.add(pName);
                TypeMirror pTypeName = variableElement.asType();
                parameterSpecs.add(ParameterSpec.builder(TypeName.get(pTypeName), pName).build());
                parametersArrayPosString.append(", (").append(pTypeName).append(") objects[").append(i).append("]");
                parametersNString.append(", $N");
                parametersString.append(", ").append(pName);
            }
        }

        if (asyncType == AsyncType.RX) {
            ParameterizedTypeName rxReturnType = ParameterizedTypeName.get(observableCN, returnTypeName);
            MethodSpec.Builder methodAsync = MethodSpec.methodBuilder(methodName + "Rx")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameterSpecs)
                    .returns(rxReturnType);

            CodeBlock.Builder methodAsyncBody = CodeBlock.builder();

            if (isVoid) {
                methodAsyncBody.addStatement("$T observable = $T.fromAction(() -> $N(" + parametersString + ")).toObservable()",
                        rxReturnType, completableCN, methodName);
            } else {
                methodAsyncBody.addStatement("$T observable = $T.fromCallable(() -> $N(" + parametersString + "))",
                        rxReturnType, observableCN, methodName);
            }
            methodAsyncBody.addStatement("return observable.subscribeOn($T.io())" +
                    ".observeOn($T.mainThread())", schedulersCN, androidSchedulersCN);
            methodAsync.addCode(methodAsyncBody.build());
            navigatorClass.addMethod(methodAsync.build());
            return;
        }
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
                taskStatusClassName,
                pair
        );
        MethodSpec.Builder doInBackgroundMethod = MethodSpec.methodBuilder("doInBackground")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Object[].class, "objects").varargs();
        CodeBlock.Builder doInBackgroundMethodBody = CodeBlock.builder()
                .addStatement("taskStatus.setState($T.LOADING)", stateClassName)
                .addStatement("publishProgress(taskStatus)");
        if (isVoid) {
            doInBackgroundMethodBody
                    .beginControlFlow("try")
                    .addStatement("this.classInstance.$N($N)", methodName, parametersArrayPosString.toString())
                    .addStatement("return new $T(null, null)", pair)
                    .nextControlFlow("catch ($T e)", ClassName.get(Throwable.class))
                    .addStatement("return new $T(e, null)", pair)
                    .endControlFlow();
        } else {
            doInBackgroundMethodBody
                    .beginControlFlow("try")
                    .addStatement("$T result = this.classInstance.$N($N)", returnType, methodName, parametersArrayPosString.toString())
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
                        .addStatement("taskStatus.setState(State.ERROR)")
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
                        .addStatement("taskStatus.setValue(result.second)")
                        .addStatement("taskStatus.finish(result.first == null " +
                                "? State.SUCCEEDED " +
                                ": State.ERROR, result.first)")
                        .addStatement("publishProgress(taskStatus)")
                        .build());

        MethodSpec.Builder onProgressUpdateMethod = MethodSpec.methodBuilder("onProgressUpdate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ArrayTypeName.of(taskStatusClassName), "state").varargs()
                .addStatement("if (callback != null) callback.onStateChanged(state[0])");
        TypeName taskStatusType = ParameterizedTypeName.get(taskStatusClassName, returnTypeName);
        TypeName taskStatusLiveDataType = ParameterizedTypeName.get(ClassName.get(lifecyclePack, "MutableLiveData"), taskStatusType);

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(type), "classInstance")
                .addCode(CodeBlock.builder()
                        .addStatement("this.classInstance = classInstance")
                        .addStatement("publishProgress(taskStatus)")
                        .build());

        ParameterizedTypeName callbackType = ParameterizedTypeName.get(callbackClassName, returnTypeName);
        FieldSpec.Builder callback = FieldSpec.builder(callbackType, "callback", Modifier.PRIVATE, Modifier.FINAL);

        FieldSpec.Builder taskStatus =
                FieldSpec.builder(taskStatusType, "taskStatus", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T(this.getClass().getSimpleName())", taskStatusClassName);
        FieldSpec.Builder taskStatusLiveData =
                FieldSpec.builder(taskStatusLiveDataType, "liveData", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new MutableLiveData<>()", taskStatusClassName);

        ClassName methodTaskClassName = className.nestedClass(methodTaskName);
        TypeSpec.Builder methodTaskClass = TypeSpec.classBuilder(methodTaskClassName.simpleName());
        if (asyncType == AsyncType.ASYNC_TASK) {
            constructor.addParameter(callbackType, "callback");
            constructor.addCode(CodeBlock.builder()
                    .addStatement("this.callback = callback")
                    .build());
        } else if (asyncType == AsyncType.LIVE_DATA) {
            constructor.addCode(CodeBlock.builder()
                    .addStatement(
                            "this.callback = new Callback<$T>() {\n" +
                                    "        @Override\n" +
                                    "        public void onStateChanged(TaskStatus status) {\n" +
                                    "           liveData.setValue(status);\n" +
                                    "        }\n" +
                                    "      }", returnTypeName)
                    .build());

            methodTaskClass.addMethod(MethodSpec.methodBuilder("asLiveData")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return liveData")
                    .returns(taskStatusLiveDataType)
                    .build());
        }

        methodTaskClass
                .superclass(asyncTask)
                .addModifiers(Modifier.STATIC)
                .addField(FieldSpec.builder(TypeName.get(type), "classInstance")
                        .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                        .build())
                .addField(taskStatus.build())
                .addField(taskStatusLiveData.build())
                .addField(callback.build())
                .addMethod(constructor.build())
                .addMethod(onCancelledMethod.build())
                .addMethod(onPreExecuteMethod.build())
                .addMethod(doInBackgroundMethod.build())
                .addMethod(onPostExecuteMethod.build())
                .addMethod(onProgressUpdateMethod.build());

        navigatorClass.addType(methodTaskClass.build());

        MethodSpec.Builder methodAsync = MethodSpec.methodBuilder(methodName + "Async")
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecs);
        if (asyncType == AsyncType.ASYNC_TASK) {
            methodAsync.returns(methodTaskClassName);
            methodAsync.addParameter(callbackType, "callback");
        } else if (asyncType == AsyncType.LIVE_DATA) {
            TypeName liveData = ParameterizedTypeName.get(ClassName.get(lifecyclePack, "LiveData"),
                    taskStatusType
            );
            methodAsync.returns(liveData);
        }

        CodeBlock.Builder methodAsyncBody = CodeBlock.builder();
        if (!allowMultipleCalls) {
            methodAsyncBody.addStatement("$T taskG = taskMap.get($T.class.getSimpleName())",
                    ClassName.get("android.os", "AsyncTask"),
                    methodTaskClassName);
            methodAsyncBody.beginControlFlow("if (taskG != null && taskG.getStatus() != AsyncTask.Status.FINISHED)");

            if (asyncType == AsyncType.ASYNC_TASK) {
                methodAsyncBody.addStatement("return (($T) taskG)", methodTaskClassName);
            } else if (asyncType == AsyncType.LIVE_DATA) {
                methodAsyncBody.addStatement("return (($T) taskG).asLiveData()", methodTaskClassName);
            }
            methodAsyncBody.endControlFlow();
        }
        if (asyncType == AsyncType.ASYNC_TASK) {
            methodAsyncBody.addStatement("$T task = new $T(this, callback)", methodTaskClassName, methodTaskClassName);
        } else if (asyncType == AsyncType.LIVE_DATA) {
            methodAsyncBody.addStatement("$T task = new $T(this)", methodTaskClassName, methodTaskClassName);
        }
        if (executorType == ExecutorType.SERIAL) {
            if (parameterNames.size() > 0)
                methodAsyncBody.addStatement("task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, " + parametersNString + ")", parameterNames.toArray());
            else
                methodAsyncBody.addStatement("task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)");

        } else {
            if (parameterNames.size() > 0)
                methodAsyncBody.addStatement("task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, " + parametersNString + ")", parameterNames.toArray());
            else
                methodAsyncBody.addStatement("task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)");
        }

        if (!allowMultipleCalls) {
            methodAsyncBody.addStatement("taskMap.put(task.getClass().getSimpleName(), task)", methodTaskClassName, methodTaskClassName);
        }

        if (asyncType == AsyncType.ASYNC_TASK) {
            methodAsyncBody.addStatement("return task", methodTaskClassName, methodTaskClassName);
        } else if (asyncType == AsyncType.LIVE_DATA) {
            methodAsyncBody.addStatement("return task.asLiveData()", methodTaskClassName, methodTaskClassName);
        }

        methodAsync.addCode(methodAsyncBody.build());
        navigatorClass.addMethod(methodAsync.build());
    }

}
