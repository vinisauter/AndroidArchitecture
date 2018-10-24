package com.vas.architectureandroidannotationsplugin;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by user liveData 20/09/2018.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
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

    public static TypeElement getSuperClass(TypeElement typeElement) {
        if (!(typeElement.getSuperclass() instanceof DeclaredType)) return null;
        DeclaredType declaredAncestor = (DeclaredType) typeElement.getSuperclass();
        return (TypeElement) declaredAncestor.asElement();
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

    public static boolean printInstances(Element typeElement) {
        return findInstance(typeElement) != null;
    }

    private static TypeElement findInstance(Element element) {
        System.out.print(" -> " + element.getSimpleName().toString());
        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            TypeElement superclass = getSuperClass(typeElement);
            return superclass != null ? findInstance(superclass) : null;
        } else if (element instanceof ExecutableElement) {
            ExecutableElement typeElement = (ExecutableElement) element;
            return findInstance(((DeclaredType) typeElement.getReturnType()).asElement());
        } else if (element instanceof TypeParameterElement) {
            TypeParameterElement typeElement = (TypeParameterElement) element;
            return findInstance(((DeclaredType) typeElement.asType()).asElement());
        } else if (element instanceof VariableElement) {
            VariableElement typeElement = (VariableElement) element;
            return findInstance(((DeclaredType) typeElement.asType()).asElement());
        }
        return null;
    }
}
