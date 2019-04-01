package com.vas.androidarchitecture.util;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

/**
 * MirrorLiveData é responsavel por armazenar as informações (nome do método + parametros) das chamadas feitas a interface {@link T}.
 *
 * @param <T> Tipo da interface
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class MirrorLiveData<T> extends LiveData<MirrorLiveData.ReflexCall<T>> {

    /**
     * @param callName       nome da tarefa a ser executada
     * @param interfaceToMap interface {@link T} para criar instancia de mapeamento {@link ReflexCall} do LiveData.
     * @return Cria um {@link LiveData} do tipo {@link ReflexCall}
     */
    public static <T> MirrorLiveData<T> create(String callName, Class<T> interfaceToMap) {
        return new MirrorLiveData<>(interfaceToMap, callName);
    }

    /**
     * ReflexCall é responsavel por armazenar o informações sobre o nome do método e arqumentos das chamadas feitas a interface mirror {@link T}.
     *
     * @param <T> Tipo da interface
     */
    public static class ReflexCall<T> {
        private String callName;
        private Method method;
        private Object[] args;

        public Method getMethod() {
            return method;
        }

        public Object[] getArgs() {
            return args;
        }

        private ReflexCall(String callName, Method method, Object[] args) {
            this.callName = callName;
            this.method = method;
            this.args = args;
        }

        /**
         * reflectOnDelegate é responsavel por fazer o mapeamento da chamada feita ao mirror {@code MirrorLiveData.setReflection()}
         * encontrar e executar o metodo na classe que implementa o {@param delegate}.
         *
         * @param delegate implementação do contrato de estados.
         */
        public Object reflectOnDelegate(T delegate) {
            System.out.print("On method invoked: " + callName + "->" + method.getName());
            Object result = null;
            try {
                result = method.invoke(delegate, args);
                System.out.println(" SUCCESS");
            } catch (Throwable e) {
                System.out.println(" ERROR");
                e.printStackTrace();
            }
            return result;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("%s->%s", callName, method.getName());
        }
    }

    public void observe(@NonNull LifecycleOwner owner, @NonNull T delegate) {
        super.observe(owner, tReflexCall -> tReflexCall.reflectOnDelegate(delegate));
    }

    public void observeForever(@NonNull T delegate) {
        super.observeForever(tReflexCall -> tReflexCall.reflectOnDelegate(delegate));
    }

    private final T mirror;

    private MirrorLiveData(Class<T> clazz, String callName) {
        //noinspection unchecked
        this.mirror = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            postValue(new ReflexCall<>(callName, method, args));
            return null;
        });
    }

    public T setReflection() {
        return mirror;
    }
}