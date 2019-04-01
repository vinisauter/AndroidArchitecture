package com.vas.architecture_processor;


import com.squareup.javapoet.ClassName;
import com.vas.architecture_processor.exceptions.AnnotationException;
import com.vas.architecture_processor.repository.ArcRepositoryGenerator;
import com.vas.architecture_processor.view.ArcViewGenerator;
import com.vas.architecture_processor.viewmodel.ArcViewModelGenerator;
import com.vas.architectureandroidannotations.Ignore;
import com.vas.architectureandroidannotations.RepositoryARC;
import com.vas.architectureandroidannotations.ViewARC;
import com.vas.architectureandroidannotations.ViewModelARC;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class ArchitectureProcessor extends AbstractProcessor {
    public static ProcessingEnvironment pEnvironment;
    private Filer filer;
    public static Messager messager;
    private Elements elements;
    private SourceVersion sourceVersion;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        pEnvironment = processingEnvironment;
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
        sourceVersion = processingEnvironment.getSourceVersion();

        Utils.logInfo("-ArchitectureProcessor-" + sourceVersion);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("\n-----------PROCESS_START-----------");
        HashMap<String, ClassName> repositories = new HashMap<>();
        HashMap<String, ClassName> viewModels = new HashMap<>();
        ArcViewGenerator arcViewGenerator = new ArcViewGenerator();
        ArcViewModelGenerator arcViewModelGenerator = new ArcViewModelGenerator();
        ArcRepositoryGenerator arcRepositoryGenerator = new ArcRepositoryGenerator();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(RepositoryARC.class)) {
            Utils.logInfo("--Generating--RepositoryARC--" + element);
            try {
                ClassName repositoryClass = arcRepositoryGenerator.generateClass(messager, filer, element);
                repositories.put(repositoryClass.simpleName(), repositoryClass);
                String pack = Utils.getPackage(element).toString();
                String name = element.getSimpleName().toString();
                ClassName className = ClassName.get(pack, name);
                repositories.put(name, className);
            } catch (AnnotationException ae) {
                Utils.logError(ae.getMessage());
            } catch (IOException ignored) {
            }
        }
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ViewModelARC.class)) {
            Utils.logInfo("--Generating--ViewModelARC--" + element);
            try {
                ClassName viewModelClass = arcViewModelGenerator.generateClass(messager, filer, element, repositories);
                viewModels.put(viewModelClass.simpleName(), viewModelClass);
                String pack = Utils.getPackage(element).toString();
                String name = element.getSimpleName().toString();
                ClassName className = ClassName.get(pack, name);
                viewModels.put(name, className);
            } catch (AnnotationException ae) {
                Utils.logError(ae.getMessage());
            } catch (IOException ignored) {
            }
        }
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ViewARC.class)) {
            Utils.logInfo("--Generating-ArcView--" + element);
            try {
                arcViewGenerator.generateClass(messager, filer, element, viewModels, arcViewModelGenerator.getLiveDataClass());
            } catch (AnnotationException ae) {
                Utils.logError(ae.getMessage());
            } catch (IOException ignored) {
            }
        }

        try {
            Utils.generateDefaultClasses();
        } catch (IOException ignored) {
        } catch (Exception ae) {
            Utils.logError(ae.getMessage());
        }
        System.out.println("------------PROCESS_END------------\n");
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        Collections.addAll(types,
//                ViewARC.class.getCanonicalName(),
                ViewModelARC.class.getCanonicalName(),
                RepositoryARC.class.getCanonicalName(),
                Ignore.class.getCanonicalName()
        );
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
