package com.vas.architecture_processor;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Created by user liveData 20/09/2018.
 */
@SuppressWarnings("ALL")
public class Utils {
    public static final String lifecyclePack = "androidx.lifecycle";
    public static final ClassName callbackClassName =
            ClassName.get("com.vas.architectureandroidannotations.api", "Callback");
    public static final ClassName taskStatusClassName =
            ClassName.get("com.vas.architectureandroidannotations.api", "TaskStatus");
    public static final ClassName stateClassName =
            ClassName.get("com.vas.architectureandroidannotations.api", "State");

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
     * @param typeMirror
     * @param classCanonicalName Full Identifier of the class {@code "java.util.Map"}{@link Map}, inner class {@code "java.util.Map.Entry"} {@link Map.Entry}
     * @return returns true if {@param typeElement} is instanceOf {@param classCanonicalName}
     */
    public static boolean instanceOf(TypeMirror typeMirror, String classCanonicalName) {
        if (typeMirror.getKind().isPrimitive())
            return classCanonicalName.equals(typeMirror.toString());
        else {
            Element element = ArchitectureProcessor.pEnvironment.getTypeUtils().asElement(typeMirror);
            return element != null && instanceOf(element, classCanonicalName);
        }
    }

    /**
     * @param element
     * @param classCanonicalName Full Identifier of the class {@code "java.util.Map"}{@link Map}, inner class {@code "java.util.Map.Entry"} {@link Map.Entry}
     * @return returns true if {@param typeElement} is instanceOf {@param classCanonicalName}
     */
    public static boolean instanceOf(Element element, String classCanonicalName) {
        if (element.asType().getKind().isPrimitive())
            return classCanonicalName.equals(element.asType().toString());
        else return findInstanceOf(element, classCanonicalName) != null;
    }

    public static TypeElement findInstanceOf(Element element, String classCanonicalName) {
        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            if (typeElement.toString().equals(classCanonicalName)) {
                return typeElement;
            } else {
                for (TypeMirror elementInterface : typeElement.getInterfaces()) {
                    if (elementInterface.toString().equals(classCanonicalName)) {
                        return (TypeElement) ((DeclaredType) elementInterface).asElement();
                    }
                }
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

    private static TypeElement findInstanceOf(TypeElement typeElement, String classCanonicalName) {
        TypeElement superclass = getSuperClass(typeElement);
        return superclass != null ?
                superclass.toString().equals(classCanonicalName) ? superclass : findInstanceOf(superclass, classCanonicalName)
                : null;
    }

    public static TypeMirror[] getGenericType(final TypeMirror type) {
        ArrayList<TypeMirror> result = new ArrayList<>();

        type.accept(new SimpleTypeVisitor6<Void, Void>() {
            @Override
            public Void visitDeclared(DeclaredType declaredType, Void v) {
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    for (int i = 0; i < typeArguments.size(); i++) {
                        result.add(typeArguments.get(i));
                    }
                }
                return null;
            }

            @Override
            public Void visitPrimitive(PrimitiveType primitiveType, Void v) {
                return null;
            }

            @Override
            public Void visitArray(ArrayType arrayType, Void v) {
                return null;
            }

            @Override
            public Void visitTypeVariable(TypeVariable typeVariable, Void v) {
                return null;
            }

            @Override
            public Void visitError(ErrorType errorType, Void v) {
                return null;
            }

            @Override
            protected Void defaultAction(TypeMirror typeMirror, Void v) {
                throw new UnsupportedOperationException();
            }
        }, null);

        return result.toArray(new TypeMirror[0]);
    }

    public static void generateDefaultClasses() throws Exception {

    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void logInfo(String log) {
        System.out.println(ANSI_RESET + "ARC_INFO:  " + log);
    }

    public static void logWarn(String log) {
        System.out.println(ANSI_YELLOW + "ARC_WARN:  " + log + ANSI_RESET);
    }

    public static void logError(String log) {
        System.out.println(ANSI_RED + "ARC_ERROR:  " + log + ANSI_RESET);
    }
}
