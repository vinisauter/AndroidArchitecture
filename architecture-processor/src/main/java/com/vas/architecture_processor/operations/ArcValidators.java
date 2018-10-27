package com.vas.architecture_processor.operations;


import com.vas.architecture_processor.exceptions.AnnotationException;
import com.vas.architectureandroidannotations.ArcViewModel;
import com.vas.architectureandroidannotations.Ignore;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Created by user liveData 04/10/2018.
 */

public class ArcValidators {
    public static void validateClass(Element elementBase) throws AnnotationException {
        // get annotation of the specified type if such an annotation is present, else null.
        ArcViewModel annotation = elementBase.getAnnotation(ArcViewModel.class);
        String value = annotation.value();
        if (elementBase.getKind() != CLASS) {
            throw new AnnotationException("Can only be applied to class.");
        }
        TypeElement typeElement = (TypeElement) elementBase;
//        boolean isObject = Utils.instanceOf(typeElement, "java.lang.Object");
        if (elementBase.getModifiers().contains(PRIVATE)) {
            throw new AnnotationException(MessageFormat.format("{0} {1} may not be applied to private classes. ({2})", ArcViewModel.class.getSimpleName(), typeElement.getQualifiedName(), elementBase.getSimpleName()));
        }
    }

    public static void validateField(Element elementEnclosed) throws AnnotationException {
        Ignore ignore = elementEnclosed.getAnnotation(Ignore.class);
        ElementKind fieldKind = elementEnclosed.getKind();
        Set<Modifier> fieldModifiers = elementEnclosed.getModifiers();
        System.out.printf(MessageFormat.format(
                "\n    EnclosedElement {0} {1} {2} {3} {4} {5}",
                fieldKind,
                ignore != null ? "ignore" : " - ",
                Arrays.toString(fieldModifiers.toArray()),
                elementEnclosed.getSimpleName().toString(),
                elementEnclosed.asType(),
                elementEnclosed.asType().getKind().isPrimitive() ? "primitive" : ""
        ));
        if (ignore == null) {
            //TODO: validateField
        }
    }

    public static void validateMethod(Element elementEnclosed) {
        Ignore ignore = elementEnclosed.getAnnotation(Ignore.class);
        ElementKind fieldKind = elementEnclosed.getKind();
        Set<Modifier> fieldModifiers = elementEnclosed.getModifiers();
        System.out.printf(MessageFormat.format(
                "\n    EnclosedElement {0} {1} {2} {3} {4} {5}",
                fieldKind,
                ignore != null ? "ignore" : " - ",
                Arrays.toString(fieldModifiers.toArray()),
                elementEnclosed.getSimpleName().toString(),
                elementEnclosed.asType(),
                elementEnclosed.asType().getKind().isPrimitive() ? "primitive" : ""
        ));
        if (ignore == null) {
            //TODO: validateMethod
        }
    }
}
