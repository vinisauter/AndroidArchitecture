package com.vas.architectureandroidannotationsplugin;

import com.vas.architectureandroidannotationsplugin.handlers.ToStringHandler;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.handler.AnnotationHandler;
import org.androidannotations.plugin.AndroidAnnotationsPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vinicius Sauter liveData 19.06.2016.
 */
public class ArcAndroidAnnotationsPlugin extends AndroidAnnotationsPlugin {

    @Override
    public String getName() {
        return "architectureandroidannotationsplugin";
    }

    @Override
    public List<AnnotationHandler<?>> getHandlers(AndroidAnnotationsEnvironment androidAnnotationEnv) {
        List<AnnotationHandler<?>> handlers = new ArrayList<>();
        handlers.add(new ToStringHandler(androidAnnotationEnv));
//        handlers.add(new ArcViewHandler(androidAnnotationEnv));
        return handlers;
    }
}
