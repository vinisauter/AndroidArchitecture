package com.vas.arcandroidannotationsplugin;

import com.vas.arcandroidannotationsplugin.handler.ViewHandler;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.handler.AnnotationHandler;
import org.androidannotations.plugin.AndroidAnnotationsPlugin;

import java.util.ArrayList;
import java.util.List;

//public class AndroidArcPlugin extends CorePlugin {
//    @Override
//    public List<AnnotationHandler<?>> getHandlers(AndroidAnnotationsEnvironment androidAnnotationEnv) {
//        List<AnnotationHandler<?>> handlers = super.getHandlers(androidAnnotationEnv);
//        handlers.add(new ViewHandler(androidAnnotationEnv));
//        return handlers;
//    }
//}

public class AndroidArcPlugin extends AndroidAnnotationsPlugin {

    @Override
    public String getName() {
        return "arcandroidannotationsplugin";
    }

    @Override
    public List<AnnotationHandler<?>> getHandlers(AndroidAnnotationsEnvironment androidAnnotationEnv) {
        List<AnnotationHandler<?>> handlers = new ArrayList<>();
        handlers.add(new ViewHandler(androidAnnotationEnv));
        return handlers;
    }
}