package com.vas.androidarchitecture.view;

import android.widget.Toast;

import com.vas.androidarchitecture.R;
import com.vas.androidarchitecture.model.User;
import com.vas.androidarchitecture.viewmodel.ConfigViewModelARC;
import com.vas.androidarchitecture.viewmodel.MainViewModelARC;
import com.vas.architectureandroidannotations.ViewARC;
import com.vas.architectureandroidannotations.view.ObserveData;
import com.vas.architectureandroidannotations.view.ViewModel;

import org.androidannotations.annotations.EFragment;

import java.text.MessageFormat;

import androidx.fragment.app.Fragment;

@ViewARC(useSharedVM = false)
@EFragment(R.layout.fragment_blank)
public class BlankFragment extends Fragment {

    @ViewModel("1")
    MainViewModelARC viewVM;
    @ViewModel("2")
    ConfigViewModelARC configVM;

    @ObserveData
    void onUserChanged(User currentUser) {
        if (currentUser != null)
            Toast.makeText(getContext(),
                    MessageFormat.format("{0} {1}", getClass().getSimpleName(), currentUser.toString()),
                    Toast.LENGTH_LONG).show();
    }
}
