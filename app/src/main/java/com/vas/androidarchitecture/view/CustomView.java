package com.vas.androidarchitecture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.vas.androidarchitecture.model.User;
import com.vas.androidarchitecture.viewmodel.ConfigViewModelARC;
import com.vas.androidarchitecture.viewmodel.MainViewModelARC;
import com.vas.architectureandroidannotations.ViewARC;
import com.vas.architectureandroidannotations.view.ObserveData;
import com.vas.architectureandroidannotations.view.ViewModel;

import org.androidannotations.annotations.EView;

import java.text.MessageFormat;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

/**
 * Created by Vinicius Sauter on 07/01/19.
 */
@ViewARC
@EView
public class CustomView extends View implements LifecycleOwner {

    @ViewModel("1")
    MainViewModelARC viewVM;
    @ViewModel("2")
    ConfigViewModelARC configVM;


    LifecycleRegistry mLifecycleRegistry;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLifecycleRegistry = new LifecycleRegistry(this);
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    @ObserveData
    void onUserChanged(User currentUser) {
        if (currentUser != null)
            Toast.makeText(getContext(),
                    MessageFormat.format("{0}" +
                            " {1}", getClass().getSimpleName(), currentUser.toString()),
                    Toast.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }
}