package com.vas.architectureandroidannotationsplugin.validators;

import com.vas.architectureandroidannotations.Ignore;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 * Created by Vinicius Sauter liveData 10/10/2018.
 */

public class ArcViewValidator {

    public static void validateElementField(Element element) {
        Ignore ignore = element.getAnnotation(Ignore.class);
        if (ignore == null) {
            //TODO:
            ElementKind fieldKind = element.getKind();
            Set<Modifier> fieldModifiers = element.getModifiers();
            System.out.printf(MessageFormat.format("\n    EnclosedElement {0} {1} {2} {3} {4} {5}",
                    fieldKind,
                    Arrays.toString(fieldModifiers.toArray()),
                    element.getSimpleName().toString(),
                    element.asType(),
                    element.asType().getKind().isPrimitive() ? "primitive" : ""
            ));
        }
    }

    public static void validateElementMethod(Element element) {
        Ignore ignore = element.getAnnotation(Ignore.class);
        if (ignore == null) {
            //TODO:
            ElementKind fieldKind = element.getKind();
            Set<Modifier> fieldModifiers = element.getModifiers();
            System.out.printf(MessageFormat.format("\n    EnclosedElement {0} {1} {2} {3} {4} {5}",
                    fieldKind,
                    Arrays.toString(fieldModifiers.toArray()),
                    element.getSimpleName().toString(),
                    element.asType(),
                    element.asType().getKind().isPrimitive() ? "primitive" : ""
            ));
        }
    }
}
